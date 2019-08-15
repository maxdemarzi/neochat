package com.maxdemarzi.seed;

import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.Map;
import java.util.stream.Stream;

public class Intents {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.seed.intents", mode = Mode.WRITE)
    @Description("CALL com.maxdemarzi.seed.intents()")
    public Stream<StringResult> seedIntents() {

        String greeting =
                "CREATE (i1:Intent {id:'greeting'})" +
                "CREATE (i1r1:Response {text:'Hi $name!', parameter_names:['name']})" +
                "CREATE (i1r2:Response {text:'Hello $name!', parameter_names:['name']})" +
                "CREATE (i1r3:Response {text:'Hello there!', parameter_names:[]})" +
                "CREATE (i1r4:Response {text:'Hiya!', parameter_names:[]})" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r4)";

        executeCypher(greeting);

        String complete =
                "CREATE (i1:Intent {id:'complete'})" +
                "CREATE (i1r1:Response {text:'Bye $name. Have a good $time_of_day!', parameter_names:['name', 'time_of_day']})" +
                "CREATE (i1r2:Response {text:'Bye Bye $name! Have an awesome $time_of_day', parameter_names:['name', 'time_of_day']})" +
                "CREATE (i1r3:Response {text:'Until next time! Have a great $time_of_day', parameter_names:['time_of_day']})" +
                "CREATE (i1r4:Response {text:'Have a good one! Have an excellent $time_of_day', parameter_names:['time_of_day']})" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r4)";

        executeCypher(complete);

        String category_intent =
                "CREATE (i1:Intent {id:'category_inquiry'})" +
                "CREATE (i1r1:Response {text:'Are you interested in any specific types of products?', parameter_names:[], category_size:0})" +
                "CREATE (i1r2:Response {text:'Can you be more specific?', parameter_names:[], category_size:0})" +
                "CREATE (i1r3:Response {text:'What are you really looking for?', parameter_names:[], category_size:0})" +
                "CREATE (i1r4:Response {text:'We have plenty of $categories[0], what about $products[0] or $products[1]?', parameter_names:['categories', 'products'], category_size:1})" +
                "CREATE (i1r5:Response {text:'We carry $category, like $products[0] or $products[1]?', parameter_names:['categories', 'products'], category_size:1})" +
                "CREATE (i1r6:Response {text:'That is a broad $categories[0], into $subcategories[0] or $subcategories[1]?', parameter_names:['categories', 'subcategories'], category_size:1, subcategories: true})" +
                "CREATE (i1r7:Response {text:'What type of $categories[0], like $subcategories[0] or $subcategories[1]?', parameter_names:['categories', 'subcategories'], category_size:1, subcategories: true})" +
                "CREATE (i1r8:Response {text:'What do you want to talk about first? $categories[0] or $categories[1]', parameter_names:['categories'], category_size:2})" +
                "CREATE (i1r9:Response {text:'Lets narrow it down? $categories[0] or $categories[1]', parameter_names:['categories'], category_size:2})" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r4)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r5)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r6)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r7)" ;

        executeCypher(category_intent);

        return Stream.of(new StringResult("Seeded Intents"));
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
