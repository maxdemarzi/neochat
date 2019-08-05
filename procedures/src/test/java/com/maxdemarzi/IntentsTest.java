package com.maxdemarzi;

import org.junit.jupiter.api.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

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

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            session.run( "CALL com.maxdemarzi.train" );
            StatementResult result = session.run( "CALL com.maxdemarzi.intents($text)",
                    parameters( "text", "Hello?" ) );

            // Then I should get what I expect
            assertThat(result.single().get("intent").asString()).isEqualTo("greeting");
        }
    }

    private static final String MODEL_STATEMENT = "";
    ;
}
