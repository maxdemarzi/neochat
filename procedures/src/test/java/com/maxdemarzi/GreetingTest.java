package com.maxdemarzi;

import com.maxdemarzi.seed.Decisions;
import org.junit.jupiter.api.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class GreetingTest {

    private static ServerControls neo4j;

    @BeforeAll
    static void startNeo4j() {
        neo4j = TestServerBuilders.newInProcessBuilder()
                .withProcedure(Procedures.class)
                .withProcedure(Decisions.class)
                .withFixture(MODEL_STATEMENT)
                .newServer();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4j.close();
    }

    @Test
    void shouldGreet()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            session.run( "CALL com.maxdemarzi.seed.decisions()");
            session.run( "CALL com.maxdemarzi.train" );
            StatementResult result = session.run( "CALL com.maxdemarzi.intents($text)",
                    parameters( "text", "Hello?" ) );

            // Then I should get what I expect
            assertThat(result.single().get("intent").asString()).isEqualTo("greeting");

            result = session.run( "CALL com.maxdemarzi.chat($id, $phone, $text)",
                    parameters( "id", "a1", "phone", "123", "text", "Hello?" ) );

            Record record = result.single();
            assertThat(record.get("intent").asString()).isEqualTo("greeting");
            assertThat(record.get("response").asString()).endsWith("Max De Marzi!");

            result = session.run( "CALL com.maxdemarzi.chat($id, $phone, $text)",
                    parameters( "id", "a2", "phone", "456", "text", "Hello?" ) );

            record = result.single();
            assertThat(record.get("intent").asString()).isEqualTo("greeting");
            assertThat(record.get("response").asString()).doesNotContain("Max");
        }
    }

    private static final String MODEL_STATEMENT =
            "CREATE (a1:Account {id:'a1'})" +
            "CREATE (m1:Member {name:'Max De Marzi', phone:'123'})" +
            "CREATE (a1)-[:HAS_MEMBER]->(m1)" +
            "CREATE (a2:Account {id:'a2'})" +
            "CREATE (m2:Member { phone:'456'})" +
            "CREATE (a2)-[:HAS_MEMBER]->(m2)";
}
