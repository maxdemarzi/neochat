package com.maxdemarzi.seed;

import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.Map;
import java.util.stream.Stream;

public class Decisions {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.seed.decisions", mode = Mode.WRITE)
    @Description("CALL com.maxdemarzi.seed.decisions()")
    public Stream<StringResult> seedDecisions() {

        String greetingTree =
                "CREATE (tree:Tree { id: 'greeting' })" +
                "CREATE (name_blank_rule:Rule { parameter_names: 'name', parameter_types:'String', expression:'name.isEmpty()' })" +
                "CREATE (answer_yes:Answer { id: 'greeting-yes' })" +
                "CREATE (answer_no:Answer { id: 'greeting-no' })" +
                "CREATE (tree)-[:HAS]->(name_blank_rule)" +
                "CREATE (name_blank_rule)-[:IS_TRUE]->(answer_yes)" +
                "CREATE (name_blank_rule)-[:IS_FALSE]->(answer_no)" +
                "CREATE (i1r1:Response {text:'Hi $name!', parameter_names:['name']})" +
                "CREATE (i1r2:Response {text:'Hello $name!', parameter_names:['name']})" +
                "CREATE (i1r3:Response {text:'Hello there!', parameter_names:[]})" +
                "CREATE (i1r4:Response {text:'Hiya!', parameter_names:[]})" +
                "CREATE (answer_no)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (answer_no)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (answer_yes)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (answer_yes)-[:HAS_RESPONSE]->(i1r4)" +
                "RETURN 'greeting tree created' AS message";

        executeCypher(greetingTree);

        String completeTree =
                "CREATE (tree:Tree { id: 'complete' })" +
                "CREATE (name_blank_rule:Rule { parameter_names: 'name', parameter_types:'String', expression:'name.isEmpty()' })" +
                "CREATE (answer_yes:Answer { id: 'complete-yes' })" +
                "CREATE (answer_no:Answer { id: 'complete-no' })" +
                "CREATE (tree)-[:HAS]->(name_blank_rule)" +
                "CREATE (name_blank_rule)-[:IS_TRUE]->(answer_yes)" +
                "CREATE (name_blank_rule)-[:IS_FALSE]->(answer_no)" +
                "CREATE (i1r1:Response {text:'Bye $name. Have a good $time_of_day!', parameter_names:['name', 'time_of_day']})" +
                "CREATE (i1r2:Response {text:'Bye Bye $name! Have an awesome $time_of_day', parameter_names:['name', 'time_of_day']})" +
                "CREATE (i1r3:Response {text:'Until next time! Have a great $time_of_day', parameter_names:['time_of_day']})" +
                "CREATE (i1r4:Response {text:'Have a good one! Have an excellent $time_of_day', parameter_names:['time_of_day']})" +
                "CREATE (answer_no)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (answer_no)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (answer_yes)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (answer_yes)-[:HAS_RESPONSE]->(i1r4)" +
                "RETURN 'complete tree created' AS message";

        executeCypher(completeTree);

        // Do we have a category, multiple categories or no categories?
        String categoryInquiryTree =
                "CREATE (tree:Tree { id: 'category_inquiry' })" +
                "CREATE (number_of_categories_rule:Rule { parameter_names: 'categories', parameter_types:'String[]', script:'switch(categories.length) { case 0: return \"OPTION_1\"; case 1: return \"OPTION_2\"; default: return \"OPTION_3\"; }' })" +
                "CREATE (category_parameter:Parameter {name:'categories', type:'String[]', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_QUESTION]->(response) WHERE 'categories' IN response.parameter_names WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\", expression:'categories.size() > 0'})" +
                "CREATE (number_of_categories_rule)-[:REQUIRES]->(category_parameter)" +
                "CREATE (tree)-[:HAS]->(number_of_categories_rule)" +
                "CREATE (has_subcategories_rule:Rule { parameter_names: 'subcategories', parameter_types:'String[]', expression:'subcategories.length > 0' })" +

                "CREATE (zero:Answer { id: 'category_inquiry-zero' })" +
                "CREATE (one:Answer { id: 'category_inquiry-one', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size = 1 AND response.subcategories = false WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (more_than_one:Answer { id: 'category_inquiry-more_than_one', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size > 1 WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (be_specific:Answer { id: 'category_inquiry-be_specific', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size = 1 AND response.subcategories = true WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +

                "CREATE (number_of_categories_rule)-[:OPTION_1]->(zero)" +
                "CREATE (number_of_categories_rule)-[:OPTION_2]->(has_subcategories_rule)" +
                "CREATE (number_of_categories_rule)-[:OPTION_3]->(more_than_one)" +

                "CREATE (has_subcategories_rule)-[:IS_TRUE]->(be_specific)" +
                "CREATE (has_subcategories_rule)-[:IS_FALSE]->(one)" +
                "CREATE (i1r1:Response {text:'Are you interested in any specific types of products?' })" +
                "CREATE (i1r2:Response {text:'Can you be more specific?' })" +
                "CREATE (i1r3:Response {text:'What are you really looking for?' })" +
                "CREATE (i1r4:Response {text:'We have plenty of $categories[0], what about $products[0] or $products[1]?', parameter_names:['categories', 'products'] })" +
                "CREATE (i1r5:Response {text:'We carry $categories[0], like $products[0] or $products[1]?', parameter_names:['categories', 'products'] })" +
                "CREATE (i1r6:Response {text:'$categories[0] is a kinda broad, into $subcategories[0] or $subcategories[1]?', parameter_names:['categories', 'subcategories'] })" +
                "CREATE (i1r7:Response {text:'What type of $categories[0], like $subcategories[0] or $subcategories[1]?', parameter_names:['categories', 'subcategories'] })" +
                "CREATE (i1r8:Response {text:'What do you want to talk about first? $categories[0] or $categories[1]', parameter_names:['categories'] })" +
                "CREATE (i1r9:Response {text:'Lets narrow it down? $categories[0] or $categories[1]', parameter_names:['categories'] })" +
                "CREATE (zero)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (zero)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (zero)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (one)-[:HAS_RESPONSE]->(i1r4)" +
                "CREATE (one)-[:HAS_RESPONSE]->(i1r5)" +
                "CREATE (be_specific)-[:HAS_RESPONSE]->(i1r6)" +
                "CREATE (be_specific)-[:HAS_RESPONSE]->(i1r7)" +
                "CREATE (more_than_one)-[:HAS_RESPONSE]->(i1r8)" +
                "CREATE (more_than_one)-[:HAS_RESPONSE]->(i1r9)" +
                "RETURN 'category_inquiry tree created' AS message";

        executeCypher(categoryInquiryTree);

        return Stream.of(new StringResult("Seeded Decisions"));
    }

    private void executeCypher(String intent) {
        try ( Result result = db.execute( intent ) ) {
            while ( result.hasNext() )
            {
                Map<String, Object> row = result.next();
                for ( String key : result.columns() ) {
                    log.debug( "%s = %s%n", key, row.get( key ) );
                }
            }
        }
    }
}
