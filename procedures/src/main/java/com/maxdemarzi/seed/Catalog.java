package com.maxdemarzi.seed;

import com.maxdemarzi.results.IntentResult;
import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.HashMap;
import java.util.stream.Stream;

public class Catalog {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.seed.catalog", mode = Mode.WRITE)
    @Description("CALL com.maxdemarzi.seed.catalog()")
    public Stream<StringResult> seedCatalog() {

        HashMap<String, Object> weapons = new HashMap<>();
        weapons.put("file","file:///weapons.xml");
        weapons.put("path", "/chummer/categories/category");

        db.execute("CALL apoc.load.xml($file, $path) YIELD value WITH DISTINCT value.blackmarket AS top_name, value._text AS sub_name " +
                        "MERGE (top:Category {name: top_name})" +
                        "MERGE (sub:Category {name: sub_name})" +
                        "MERGE (top)<-[:IN_CATEGORY]-(sub)", weapons);

        return Stream.of(new StringResult("Seeded Catalog"));
    }
}
