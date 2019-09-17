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
                "CREATE (answer_yes:Answer { id: 'yes', query:\"MATCH (intent:Intent {id:'greeting'})-[:HAS_RESPONSE]->(response) WHERE NOT 'name' IN response.parameter_names WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (answer_no:Answer { id: 'no', query:\"MATCH (intent:Intent {id:'greeting'})-[:HAS_RESPONSE]->(response) WHERE 'name' IN response.parameter_names WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (tree)-[:HAS]->(name_blank_rule)" +
                "CREATE (name_blank_rule)-[:IS_TRUE]->(answer_yes)" +
                "CREATE (name_blank_rule)-[:IS_FALSE]->(answer_no)" +
                "RETURN 'greeting tree created' AS message";

        executeCypher(greetingTree);

        String completeTree =
                "CREATE (tree:Tree { id: 'complete' })" +
                "CREATE (name_blank_rule:Rule { parameter_names: 'name', parameter_types:'String', expression:'name.isEmpty()' })" +
                "CREATE (answer_yes:Answer { id: 'yes', query:\"MATCH (intent:Intent {id:'complete'})-[:HAS_RESPONSE]->(response) WHERE NOT 'name' IN response.parameter_names WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (answer_no:Answer { id: 'no', query:\"MATCH (intent:Intent {id:'complete'})-[:HAS_RESPONSE]->(response) WHERE 'name' IN response.parameter_names WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (tree)-[:HAS]->(name_blank_rule)" +
                "CREATE (name_blank_rule)-[:IS_TRUE]->(answer_yes)" +
                "CREATE (name_blank_rule)-[:IS_FALSE]->(answer_no)" +
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

                "CREATE (zero:Answer { id: 'zero', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size = 0 WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (one:Answer { id: 'one', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size = 1 AND response.subcategories = false WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (more_than_one:Answer { id: 'more_than_one', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size > 1 WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +
                "CREATE (be_specific:Answer { id: 'be_specific', query:\"MATCH (intent:Intent {id:'category_inquiry'})-[:HAS_RESPONSE]->(response) WHERE response.category_size = 1 AND response.subcategories = true WITH response, rand() AS r ORDER BY r RETURN response.text AS value LIMIT 1\"})" +

                "CREATE (number_of_categories_rule)-[:OPTION_1]->(zero)" +
                "CREATE (number_of_categories_rule)-[:OPTION_2]->(has_subcategories_rule)" +
                "CREATE (number_of_categories_rule)-[:OPTION_3]->(more_than_one)" +

                "CREATE (has_subcategories_rule)-[:IS_TRUE]->(be_specific)" +
                "CREATE (has_subcategories_rule)-[:IS_FALSE]->(one)" +
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
