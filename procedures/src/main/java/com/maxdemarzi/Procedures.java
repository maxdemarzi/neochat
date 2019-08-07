package com.maxdemarzi;

import com.maxdemarzi.decisions.DecisionTreeEvaluator;
import com.maxdemarzi.decisions.DecisionTreeExpander;
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
import java.util.*;
import java.util.stream.Stream;

import static com.maxdemarzi.schema.Properties.*;

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
    private static List<TokenNameFinderModel> tokenNameFinderModels;
    private static List<NameFinderME> nameFinderMEs;


    @Procedure(name = "com.maxdemarzi.chat", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.chat(String id, String text)")
    public Stream<IntentResult> chat(@Name(value = "id") String id, @Name(value = "text") String text) {
        ArrayList<IntentResult> results = new ArrayList<>();
        findIntents(text, results);

        Node account = db.findNode(Labels.Account, ID, id);

        for (IntentResult result : results) {
            switch (result.intent) {
                case "greeting":
                    greetingAction(account, result);
                    break;
                case "complete":
                    completeAction(account, result);
                    break;
            }
        }

        return results.stream();
    }

    private void completeAction(Node account, IntentResult result) {
    }

    private void greetingAction(Node account, IntentResult result) {
        // Which Decision Tree are we interested in?
        Node tree = db.findNode(Labels.Tree, ID, result.intent);
        if ( tree != null) {
            // Find the facts
            Map<String, Object> facts = new HashMap<>();
            facts.put("account_node_id", account.getId());
            Result factResult = db.execute("MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE ID(a) = $account_node_id RETURN 'name' AS key, COALESCE(member.name, '') AS value", facts);
            Map<String, Object> factMap = factResult.next();
            facts.put((String)factMap.get("key"), factMap.get("value"));

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

            for (Map.Entry<String, Object> entry : facts.entrySet()) {
                String key = "\\$" + entry.getKey();
                response = response.replaceAll(key, entry.getValue().toString() );
            }

            result.setResponse(response);
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
    @Description("CALL com.maxdemarzi.train()")
    public Stream<StringResult> train() {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();

            // Initialize the tokenizer
            InputStream modelIn = new FileInputStream(classLoader.getResource("data/models/en-token.bin").getFile());
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(model);

            // Initialize the sentencizer
            modelIn =  new FileInputStream(classLoader.getResource("data/models/en-sent.bin").getFile());
            SentenceModel sentenceModel = new SentenceModel(modelIn);
            sentencizer = new SentenceDetectorME(sentenceModel);

            // Initialieze the partOfSpeecher
            modelIn =  new FileInputStream(classLoader.getResource("data/models/en-pos-maxent.bin").getFile());
            POSModel posModel = new POSModel(modelIn);
            partOfSpeecher = new POSTaggerME(posModel);

            // Initialize the lemmatizer
            modelIn =  new FileInputStream(classLoader.getResource("data/models/en-lemmatizer.bin").getFile());
            LemmatizerModel lemmaModel = new LemmatizerModel(modelIn);
            lemmatizer = new LemmatizerME(lemmaModel);

            // Gather the Intents
            File trainingDirectory = new File(classLoader.getResource("data/training/intents").getFile());

            List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<>();
            for (File trainingFile : trainingDirectory.listFiles()) {
                String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
                ObjectStream<String> lineStream = null;

                lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(trainingFile), "UTF-8");

                ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
                categoryStreams.add(documentSampleStream);
            }

            ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils.concatenateObjectStream(categoryStreams);

            TrainingParameters trainingParams = new TrainingParameters();
            trainingParams.put(TrainingParameters.CUTOFF_PARAM, 0);

            DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

            DoccatModel doccatModel = DocumentCategorizerME.train("en", combinedDocumentSampleStream, trainingParams, factory);
            combinedDocumentSampleStream.close();
            categorizer = new DocumentCategorizerME(doccatModel);

            // Initialize TokenFinders
            tokenNameFinderModels = new ArrayList<>();

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
                tokenNameFinderModels.add(tokenNameFinderModel);
            }

            nameFinderMEs = new ArrayList<>();
            for (TokenNameFinderModel tokenNameFinderModel : tokenNameFinderModels) {
                nameFinderMEs.add(new NameFinderME(tokenNameFinderModel));
            }

            // Add date NER model
            modelIn = new FileInputStream(classLoader.getResource("data/models/en-ner-date.bin").getFile());
            TokenNameFinderModel dateModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(dateModel));

            // Add money NER model
            modelIn = new FileInputStream(classLoader.getResource("data/models/en-ner-money.bin").getFile());
            TokenNameFinderModel moneyModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(moneyModel));

            // Add person NER model
            modelIn = new FileInputStream(classLoader.getResource("data/models/en-ner-person.bin").getFile());
            TokenNameFinderModel personModel = new TokenNameFinderModel(modelIn);
            nameFinderMEs.add(new NameFinderME(personModel));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Stream.of(new StringResult("Training Complete!"));
    }

}