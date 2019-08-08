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

        String intent =
                "CREATE (i1:Intent {id:'greeting'})" +
                "CREATE (i1r1:Response {text:'Hi $name!', parameter_names:['name']})" +
                "CREATE (i1r2:Response {text:'Hello $name!', parameter_names:['name']})" +
                "CREATE (i1r3:Response {text:'Hello there!', parameter_names:[]})" +
                "CREATE (i1r4:Response {text:'Hiya!', parameter_names:[]})" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r1)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r2)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r3)" +
                "CREATE (i1)-[:HAS_RESPONSE]->(i1r4)";

        try ( Result result = db.execute( intent ) ) {
            while ( result.hasNext() )
            {
                Map<String, Object> row = result.next();
                for ( String key : result.columns() ) {
                    log.debug( "%s = %s%n", key, row.get( key ) );
                }
            }
        }

        return Stream.of(new StringResult("Seeded Intents"));
    }
}