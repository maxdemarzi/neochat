package com.maxdemarzi;

import org.junit.jupiter.api.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class IntentsTest {
    private static ServerControls neo4j;

    @BeforeAll
    static void startNeo4j() {
        neo4j = TestServerBuilders.newInProcessBuilder()
                .withProcedure(Procedures.class)
                .withFixture(MODEL_STATEMENT)
                .newServer();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4j.close();
    }

    @Test
    void shouldFindIntents()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j and trained the models
            Session session = driver.session();
            session.run( "CALL com.maxdemarzi.train" );

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.intents($text)",
                    parameters( "text", "Hello?" ) );

            // Then I should get what I expect
            assertThat(result.single().get("intent").asString()).isEqualTo("greeting");

            result = session.run( "CALL com.maxdemarzi.intents($text)",
                    parameters( "text", "show me your shotguns" ) );
            Record record = result.single();
            assertThat(record.get("intent").asString()).isEqualTo("category_inquiry");
            List<Object> args = record.get("args").asList();
            Map<String, Object> arg = (Map<String, Object>)args.get(0);
            assertThat(arg.containsKey("category"));
            assertThat(arg.get("category").toString()).isEqualTo("shotguns");
        }
    }

    private static final String MODEL_STATEMENT = "";

}
