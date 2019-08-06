package com.maxdemarzi.schema;

import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.stream.Stream;

import static com.maxdemarzi.schema.Properties.ID;
import static com.maxdemarzi.schema.Properties.NAME;

public class Schema {

    @Context
    public GraphDatabaseService db;


    @Procedure(name = "com.maxdemarzi.schema.generate", mode = Mode.SCHEMA)
    @Description("CALL com.maxdemarzi.schema.generate() - generate schema")

    public Stream<StringResult> generate() throws IOException {
        org.neo4j.graphdb.schema.Schema schema = db.schema();

        if (!schema.getIndexes(Labels.Tree).iterator().hasNext()) {
            schema.constraintFor(Labels.Tree)
                    .assertPropertyIsUnique("id")
                    .create();
        }
        if (!schema.getIndexes(Labels.Parameter).iterator().hasNext()) {
            schema.constraintFor(Labels.Parameter)
                    .assertPropertyIsUnique("name")
                    .create();
        }

        if (!schema.getConstraints(Labels.Account).iterator().hasNext()) {
            schema.constraintFor(Labels.Account)
                    .assertPropertyIsUnique(ID)
                    .create();
        }

        if (!schema.getConstraints(Labels.Category).iterator().hasNext()) {
            schema.constraintFor(Labels.Category)
                    .assertPropertyIsUnique(NAME)
                    .create();
        }

        return Stream.of(new StringResult("Schema Generated"));
    }

}