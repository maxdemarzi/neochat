package com.maxdemarzi;

import com.maxdemarzi.seed.*;
import org.junit.jupiter.api.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class CategoryInquiryTest {
    private static ServerControls neo4j;

    @BeforeAll
    static void startNeo4j() {
        neo4j = TestServerBuilders.newInProcessBuilder()
                .withProcedure(Procedures.class)
                .withProcedure(Catalog.class)
                .withProcedure(Decisions.class)
                .withFixture(MODEL_STATEMENT)
                .withProcedure(apoc.load.Xml.class)
                .withFunction(apoc.map.Maps.class)
                .withConfig("apoc.import.file.enabled", "true")
                .newServer();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4j.close();
    }

    @Test
    void shouldCategoryInquiry()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.seed.decisions()");
            assertThat(result.single().get("value").asString()).isEqualTo("Seeded Decisions");

            result = session.run( "CALL com.maxdemarzi.seed.catalog('test');");
            assertThat(result.single().get("value").asString()).isEqualTo("Seeded Catalog");

            session.run( "CALL com.maxdemarzi.train();" );
            result = session.run( "CALL com.maxdemarzi.intents($text)",
                    parameters( "text", "show me your weapons" ) );

            // Then I should get what I expect
            assertThat(result.single().get("intent").asString()).isEqualTo("category_inquiry");

            result = session.run( "CALL com.maxdemarzi.chat($id, $text)",
                    parameters( "id", "a1" ,"text", "let me see the weapons" ) );

            Record record = result.single();
            assertThat(record.get("intent").asString()).isEqualTo("category_inquiry");
            assertThat(record.get("response").asString()).contains("Weapons");

            result = session.run( "CALL com.maxdemarzi.chat($id, $text)",
                    parameters( "id", "a2" ,"text", "do you have any shotguns" ) );

            record = result.single();
            assertThat(record.get("intent").asString()).isEqualTo("category_inquiry");
            assertThat(record.get("response").asString()).contains("Shotguns");
        }
    }

    private static final String MODEL_STATEMENT =
            "CREATE (a1:Account {id:'a1'})" +
                    "CREATE (m1:Member {name:'Max De Marzi'})" +
                    "CREATE (a1)-[:HAS_MEMBER]->(m1)" +
                    "CREATE (a2:Account {id:'a2'})" +
                    "CREATE (m2:Member)" +
                    "CREATE (a2)-[:HAS_MEMBER]->(m2)"
            ;
}
