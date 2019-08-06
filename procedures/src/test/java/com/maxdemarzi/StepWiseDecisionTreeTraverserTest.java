package com.maxdemarzi;

import com.maxdemarzi.decisions.StepWiseDecisionTreeTraverser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.*;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static com.maxdemarzi.schema.Properties.ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.neo4j.driver.v1.Values.parameters;

public class StepWiseDecisionTreeTraverserTest {
    private static ServerControls neo4j;

    @BeforeAll
    static void startNeo4j() {
        neo4j = TestServerBuilders.newInProcessBuilder()
                .withProcedure(StepWiseDecisionTreeTraverser.class)
                .withFixture(MODEL_STATEMENT)
                .newServer();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4j.close();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGetAnswer()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {gender:$gender, age:$age}) yield path return path",
                    parameters( "gender", "male", "age", "20" ) );

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("no", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetAnswerTwo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {gender:$gender, age:$age}) yield path return path",
                    parameters( "gender", "female", "age", "19" ) );

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("yes", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetAnswerThree()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {gender:$gender, age:$age}) yield path return path",
                    parameters( "gender", "male", "age", "23" ) );

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("yes", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetParameter()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {}) yield path return path");

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("age", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetParameterTwo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {age:'20'}) yield path return path");

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("gender", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetParameterThree()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('bar entrance', {gender:'female'}) yield path return path");

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("age", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetScriptAnswer()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('funeral', {answer_1:$answer_1, answer_2:$answer_2, answer_3:$answer_3}) yield path return path",
                    parameters( "answer_1", "yeah", "answer_2", "yeah", "answer_3", "yeah" ));

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("incorrect", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetScriptAnswerTwo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('funeral', {answer_1:$answer_1, answer_2:$answer_2, answer_3:$answer_3}) yield path return path",
                    parameters( "answer_1", "what", "answer_2", "", "answer_3", "" ));

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("unknown", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetScriptAnswerThree()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('funeral', {answer_1:$answer_1, answer_2:$answer_2, answer_3:$answer_3}) yield path return path",
                    parameters( "answer_1", "what", "answer_2", "yeah", "answer_3", "okay" ));

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("correct", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetScriptParameter()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('funeral', {}) yield path return path");

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("answer_1", path.end().get(ID).asString());
        }
    }

    @Test
    void shouldGetScriptParameterTwo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "CALL com.maxdemarzi.stepwise.decision_tree('funeral', {answer_1:'what'}) yield path return path");

            // Then I should get what I expect
            Path path = result.single().get("path").asPath();
            assertEquals("answer_2", path.end().get(ID).asString());
        }
    }
    private static final String MODEL_STATEMENT =
            "CREATE (tree:Tree { id: 'bar entrance' })" +
                    "CREATE (over21_rule:Rule { parameter_names: 'age', parameter_types:'int', expression:'age >= 21' })" +
                    "CREATE (gender_rule:Rule { parameter_names: 'age,gender', parameter_types:'int,String', expression:'(age >= 18) && gender.equals(\"female\")' })" +
                    "CREATE (answer_yes:Answer { id: 'yes'})" +
                    "CREATE (answer_no:Answer { id: 'no'})" +
                    "CREATE (tree)-[:HAS]->(over21_rule)" +
                    "CREATE (over21_rule)-[:IS_TRUE]->(answer_yes)" +
                    "CREATE (over21_rule)-[:IS_FALSE]->(gender_rule)" +
                    "CREATE (gender_rule)-[:IS_TRUE]->(answer_yes)" +
                    "CREATE (gender_rule)-[:IS_FALSE]->(answer_no)" +
                    "CREATE (age:Parameter {id:'age', type:'int', prompt:'How old are you?', expression:'(age > 0) &&  (age < 150)'})" +
                    "CREATE (gender:Parameter {id:'gender', type:'String', prompt:'What is your gender?', expression: '\"male\".equals(gender) || \"female\".equals(gender)'} )" +
                    "CREATE (over21_rule)-[:REQUIRES]->(age)" +
                    "CREATE (gender_rule)-[:REQUIRES]->(age)" +
                    "CREATE (gender_rule)-[:REQUIRES]->(gender)" +
                    "CREATE (tree2:Tree { id: 'funeral' })" +
                    "CREATE (good_man_rule:Rule { name: 'Was Lil Jon a good man?', parameter_names: 'answer_1', parameter_types:'String', script:'switch (answer_1) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (good_man_two_rule:Rule { name: 'I said, was he a good man?', parameter_names: 'answer_2', parameter_types:'String', script:'switch (answer_2) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (rest_in_peace_rule:Rule { name: 'May he rest in peace', parameter_names: 'answer_3', parameter_types:'String', script:'switch (answer_3) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })" +
                    "CREATE (answer_correct:Answer { id: 'correct'})" +
                    "CREATE (answer_incorrect:Answer { id: 'incorrect'})" +
                    "CREATE (answer_unknown:Answer { id: 'unknown'})" +
                    "CREATE (tree2)-[:HAS]->(good_man_rule)" +
                    "CREATE (good_man_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (good_man_rule)-[:OPTION_2]->(good_man_two_rule)" +
                    "CREATE (good_man_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (good_man_two_rule)-[:OPTION_1]->(rest_in_peace_rule)" +
                    "CREATE (good_man_two_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (rest_in_peace_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_3]->(answer_correct)" +
                    "CREATE (rest_in_peace_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (parameter1:Parameter {id:'answer_1', type:'String', prompt:'What is the first answer?', expression:'\"yeah\".equals(answer_1) || \"what\".equals(answer_1) || \"okay\".equals(answer_1) || \"\".equals(answer_1)'})" +
                    "CREATE (parameter2:Parameter {id:'answer_2', type:'String', prompt:'What is the second answer?', expression:'\"yeah\".equals(answer_2) || \"what\".equals(answer_2) || \"okay\".equals(answer_2) || \"\".equals(answer_2)'})" +
                    "CREATE (parameter3:Parameter {id:'answer_3', type:'String', prompt:'What is the third answer?', expression:'\"yeah\".equals(answer_3) || \"what\".equals(answer_3) || \"okay\".equals(answer_3) || \"\".equals(answer_3)'})" +
                    "CREATE (good_man_rule)-[:REQUIRES]->(parameter1)" +
                    "CREATE (good_man_two_rule)-[:REQUIRES]->(parameter2)" +
                    "CREATE (rest_in_peace_rule)-[:REQUIRES]->(parameter3)";
}
