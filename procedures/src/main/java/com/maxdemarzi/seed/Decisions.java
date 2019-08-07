package com.maxdemarzi.seed;

import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.HashMap;
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
        try ( Result result = db.execute( greetingTree ) ) {
            while ( result.hasNext() )
            {
                Map<String, Object> row = result.next();
                for ( String key : result.columns() ) {
                    log.debug( "%s = %s%n", key, row.get( key ) );
                }
            }
        }

        return Stream.of(new StringResult("Seeded Decisions"));
    }
}
