package com.maxdemarzi;

import com.maxdemarzi.decisions.DecisionTreeEvaluator;
import com.maxdemarzi.decisions.DecisionTreeExpander;
import com.maxdemarzi.facts.FactGenerator;
import com.maxdemarzi.results.IntentResult;
import com.maxdemarzi.results.StringResult;
import com.maxdemarzi.schema.Labels;
import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.namefind.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.maxdemarzi.schema.Properties.*;
import static com.maxdemarzi.schema.RelationshipTypes.PREV_MESSAGE;

public class Procedures {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static DocumentCategorizerME categorizer;
    private static Tokenizer tokenizer;
    private static SentenceDetectorME sentencizer;
    private static POSTaggerME partOfSpeecher;
    private static LemmatizerME lemmatizer;
    private static List<NameFinderME> nameFinderMEs;

    private static Pattern brackets = Pattern.compile("\\[(.*?)\\]");

    @Procedure(name = "com.maxdemarzi.chat", mode = Mode.WRITE)
    @Description("CALL com.maxdemarzi.chat(String id, String text)")
    public Stream<IntentResult> chat(@Name(value = "id") String id, @Name(value = "text") String text) {

        // Find the account we are interested in
        Node account = db.findNode(Labels.Account, ID, id);

        // Create then next message node
        Node next = db.createNode(Labels.Message);
        next.setProperty(TEXT, text);
        next.setProperty(DATE, ZonedDateTime.now());

        // Insert the message into the Previous Message Chain
        if (account.hasRelationship(Direction.OUTGOING, PREV_MESSAGE)) {
            Relationship prev = account.getSingleRelationship(PREV_MESSAGE, Direction.OUTGOING);
            prev.delete();
            Node last = prev.getEndNode();
            next.createRelationshipTo(last, PREV_MESSAGE);
        }

        // Connect the new message at the head of the chain
        account.createRelationshipTo(next, PREV_MESSAGE);

        // TODO: Was our last response a Question?
        // if so then get the answer and respond with the last intent.
        // question type: yes/no, specific choice, entity choice (product, money, time, etc)

        ArrayList<IntentResult> results = new ArrayList<>();
        findIntents(text, results);

        // Get the Responses
        for (IntentResult result : results) {
            respond(account, result, next);
        }
        return results.stream();
    }

    private void respond(Node account, IntentResult result, Node next) {
        // Which Decision Tree are we interested in?
        Node tree = db.findNode(Labels.Tree, ID, result.intent);
        if ( tree != null) {
            // Find the facts
            FactGenerator factGenerator = new FactGenerator(db, account);
            Map<String, Object> facts = new HashMap<>();
            factGenerator.getMemberFacts(facts);
            factGenerator.getTimeFacts(facts);

            switch (result.intent) {
                case "category_inquiry": {
                    factGenerator.getCategoryFacts(result, facts);
                }
            }

            Stream<Path> paths = decisionPath(tree, facts);
            Path path = paths.findFirst().get();

            String query = (String)path.endNode().getProperty(QUERY);
            String response = "";
            try ( Result queryResult = db.execute( query ) ) {
                while (queryResult.hasNext()) {
                    Map<String, Object> row = queryResult.next();
                    response = (String)row.get("value");
                }
            }

            // Fill in facts
            for (Map.Entry<String, Object> entry : facts.entrySet()) {
                Pattern arrayFactPattern = Pattern.compile("\\$" + entry.getKey() + "\\[\\d+\\]");
                Matcher matcher = arrayFactPattern.matcher(response);
                HashMap<String, String> replacements = new HashMap<>();
                // Don't make the replacements in this while loop or you will mess up the response string.
                while(matcher.find()) {
                    String found = response.substring(matcher.start(), matcher.end());
                    String num = found.split("\\[")[1].split("\\]")[0];
                    String[] arrayFact = (String[])entry.getValue();
                    replacements.put(found, arrayFact[Integer.parseInt(num)]);
                }
                for (Map.Entry<String, String> replacement: replacements.entrySet()) {
                    response = response.replaceFirst(Pattern.quote(replacement.getKey()), replacement.getValue() );
                }

                String key = "\\$" + entry.getKey();
                response = response.replaceAll(key, entry.getValue().toString() );
            }

            result.setResponse(response);

            // Update Graph
            next.setProperty(INTENT, result.intent);
            next.setProperty(TEXT, result.response);

            // Store Args in [type, value, type, value]
            String[] args = new String[2 * result.args.size()];
            int index = 0;
            for(Map<String, Object> map : result.args) {
                for (Map.Entry<String, Object> mapEntry : map.entrySet()) {
                    args[index] = mapEntry.getKey();
                    index++;
                    args[index] = mapEntry.getValue().toString();
                    index++;
                }
            }
            next.setProperty(ARGS, args);
        }

    }

    private Stream<Path> decisionPath(Node tree, Map<String, Object> facts) {
        TraversalDescription myTraversal = db.traversalDescription()
                .depthFirst()
                .expand(new DecisionTreeExpander(facts))
                .evaluator(new DecisionTreeEvaluator(facts));

        return myTraversal.traverse(tree).stream();
    }

    @Procedure(name = "com.maxdemarzi.intents", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.intents(String text)")
    public Stream<IntentResult> intents(@Name(value = "text") String text) {
        ArrayList<IntentResult> results = new ArrayList<>();
        findIntents(text, results);
        return results.stream();
    }

    private void findIntents(String text, ArrayList<IntentResult> results) {
        String[] sentences = sentencizer.sentDetect(text);

        for (String sentence : sentences) {
            // Separate words from each sentence using tokenizer.
            String[] tokens = tokenizer.tokenize(sentence);

            // Tag separated words with POS tags to understand their grammatical structure.
            String[] posTags = partOfSpeecher.tag(tokens);

            // Lemmatize each word so that its easy to categorize.
            String[] lemmas = lemmatizer.lemmatize(tokens, posTags);

            double[] probabilitiesOfOutcomes = categorizer.categorize(lemmas);
            String category = categorizer.getBestCategory(probabilitiesOfOutcomes);

            List<Map<String, Object>> args = new ArrayList<>();
            if( !(category.equals("greeting") || category.equals("complete")) ) {
                for (NameFinderME nameFinderME : nameFinderMEs) {
                    Span[] spans = nameFinderME.find(tokens);
                    String[] names = Span.spansToStrings(spans, tokens);
                    for (int i = 0; i < spans.length; i++) {
                        HashMap<String, Object> arg = new HashMap<>();
                        arg.put(spans[i].getType(), names[i]);
                        args.add(arg);
                    }
                }
            }

            results.add(new IntentResult(category, args));
        }
    }

    @Procedure(name = "com.maxdemarzi.train", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.train(model_directory, intents_directory)")
    public Stream<StringResult> train(@Name(value = "model_directory", defaultValue = "") String modelDirectory,
                                      @Name(value = "intents_directory", defaultValue = "") String intentsDirectory) {
        try {

            String token;
            String sent;
            String maxent;
            String lemma;
            String date;
            String money;
            String person;

            if (modelDirectory.isEmpty()) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                token = classLoader.getResource("data/models/en-token.bin").getFile();
                sent =  classLoader.getResource("data/models/en-sent.bin").getFile();
                maxent = classLoader.getResource("data/models/en-pos-maxent.bin").getFile();
                lemma = classLoader.getResource("data/models/en-lemmatizer.bin").getFile();
                date = classLoader.getResource("data/models/en-ner-date.bin").getFile();
                money = classLoader.getResource("data/models/en-ner-money.bin").getFile();
                person = classLoader.getResource("data/models/en-ner-person.bin").getFile();
            } else {
                token = modelDirectory + "en-token.bin";
                sent = modelDirectory + "en-sent.bin";
                maxent = modelDirectory + "en-pos-maxent.bin";
                lemma = modelDirectory + "en-lemmatizer.bin";
                date = modelDirectory + "en-ner-date.bin";
                money = modelDirectory + "en-ner-money.bin";
                person = modelDirectory + "en-ner-person.bin";
            }

            log.info("Token Model: " + token );
            log.info("Sentences Model: " + sent );
            log.info("POS Model: " + maxent );
            log.info("Lemmatizer Model: " + lemma );
            log.info("Date Model: " + date );
            log.info("Money Model: " + money );
            log.info("Person Model: " + person );

            InputStream modelIn;

            // Initialize the sentencizer
            modelIn =  new FileInputStream(sent);
            SentenceModel sentenceModel = new SentenceModel(modelIn);
            sentencizer = new SentenceDetectorME(sentenceModel);
            log.info("Initialized the sentencizer");

            // Initialize the tokenizer
            modelIn = new FileInputStream(token);
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(model);
            log.info("Initialized the tokenizer");

            // Initialize the partOfSpeecher
            modelIn =  new FileInputStream(maxent);
            POSModel posModel = new POSModel(modelIn);
            partOfSpeecher = new POSTaggerME(posModel);
            log.info("Initialized the partOfSpeecher");

            // Initialize the lemmatizer
            modelIn =  new FileInputStream(lemma);
            LemmatizerModel lemmaModel = new LemmatizerModel(modelIn);
            lemmatizer = new LemmatizerME(lemmaModel);
            log.info("Initialized the lemmatizer");

            if (intentsDirectory.isEmpty()) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                intentsDirectory = classLoader.getResource("data/training/intents").getFile();
            }

            // Gather the Intents
            File trainingDirectory = new File(intentsDirectory);

            log.info("Intents Directory: " + intentsDirectory);

            List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<>();
            for (File trainingFile : trainingDirectory.listFiles()) {
                String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
                ObjectStream<String> lineStream = null;

                lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");

                ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
                categoryStreams.add(documentSampleStream);
            }

            ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils.concatenateObjectStream(categoryStreams);
            log.info("Combined Intent streams");

            TrainingParameters trainingParams = new TrainingParameters();
            trainingParams.put(TrainingParameters.CUTOFF_PARAM, 0);

            DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

            DoccatModel doccatModel = DocumentCategorizerME.train("en", combinedDocumentSampleStream, trainingParams, factory);
            combinedDocumentSampleStream.close();
            categorizer = new DocumentCategorizerME(doccatModel);
            log.info("Initialized the categorizer");

            // Initialize TokenFinders
            List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<>();
            nameFinderMEs = new ArrayList<>();

            HashMap<String,ArrayList<String>> slots = new HashMap<>();
            slots.put("product", new ArrayList<String>() {{
                add("price_inquiry");
                add("product_inquiry");
            }});
            slots.put("category", new ArrayList<String>() {{
                add("category_inquiry");
            }});
            slots.put("member", new ArrayList<String>() {{
                add("agree");
            }});
//            slots.add("order", new ArrayList<String>() {{
//                add("order_inquiry");
//            }});

            for (Map.Entry<String, ArrayList<String>> slot : slots.entrySet()) {
                List<ObjectStream<NameSample>> nameStreams = new ArrayList<>();
                for (File trainingFile : trainingDirectory.listFiles()) {
                    String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
                    if (slot.getValue().contains(intent)) {
                        ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");
                        ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
                        nameStreams.add(nameSampleStream);
                    }
                }
                ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils.concatenateObjectStream(nameStreams);

                TokenNameFinderModel tokenNameFinderModel = NameFinderME.train("en", slot.getKey(), combinedNameSampleStream, trainingParams, new TokenNameFinderFactory());
                combinedNameSampleStream.close();
                nameFinderMEs.add(new NameFinderME(tokenNameFinderModel));
            }

            log.info("Initialized the token finders");

            // Add date NER model
            modelIn = new FileInputStream(date);
            TokenNameFinderModel dateModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(dateModel));
            log.info("Initialized the date finder");

            // Add money NER model
            modelIn = new FileInputStream(money);
            TokenNameFinderModel moneyModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(moneyModel));
            log.info("Initialized the money finder");

            // Add person NER model
            modelIn = new FileInputStream(person);
            TokenNameFinderModel personModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(personModel));
            log.info("Initialized the person finder");

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        log.info("Training Complete!");
        return Stream.of(new StringResult("Training Complete!"));
    }

}