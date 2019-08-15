package com.maxdemarzi.seed;

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
    @Description("CALL com.maxdemarzi.seed.catalog(test)")
    public Stream<StringResult> seedCatalog(@Name(value = "test", defaultValue = "") String test) {

        String weaponsXML;

        if (test.isEmpty()){
            weaponsXML = "file:///weapons.xml";
        } else {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            weaponsXML = classLoader.getResource("data/catalog/weapons.xml").getFile();
        }

        HashMap<String, Object> weapons = new HashMap<>();
        weapons.put("file", weaponsXML);
        weapons.put("path", "/chummer/categories/category");

        db.execute("CALL apoc.load.xml($file, $path) YIELD value WITH DISTINCT value.blackmarket AS top_name, value._text AS sub_name " +
                        "MERGE (top:Category {name: top_name})" +
                        "MERGE (sub:Category {name: sub_name})" +
                        "MERGE (top)<-[:IN_CATEGORY]-(sub)", weapons);

        weapons.put("path", "/chummer/weapons/weapon");

        db.execute("CALL apoc.load.xml($file, $path) YIELD value " +
                        "WITH [attr IN value._children WHERE attr._text <> '' |  [attr._type, attr._text]] AS pairs " +
                        "WITH apoc.map.fromPairs(pairs) AS value " +
                        "CREATE (w:Product) " +
                        "SET w += value " +
                        "WITH w, value " +
                        "MATCH (c:Category {name: value.category}) " +
                        "CREATE (w)-[:IN_CATEGORY]->(c) ", weapons);

        return Stream.of(new StringResult("Seeded Catalog"));
    }
}
