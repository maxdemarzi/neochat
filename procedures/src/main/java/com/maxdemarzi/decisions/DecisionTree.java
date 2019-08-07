package com.maxdemarzi.decisions;

import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.ScriptEvaluator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

import static com.maxdemarzi.schema.Properties.*;

class DecisionTree {

    static boolean isValid(Node node, String fact) throws Exception {
        ExpressionEvaluator ee = new ExpressionEvaluator();
        ee.setExpressionType(boolean.class);

        String[] parameterNames = Magic.explode((String) node.getProperty(ID, EMPTY_STRING));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) node.getProperty(TYPE, EMPTY_STRING));

        // Fill the arguments array with their corresponding values
        Object[] arguments = {Magic.createObject(parameterTypes[0], fact)};

        // Set our parameters with their matching types
        ee.setParameters(parameterNames, parameterTypes);

        // And now we "cook" (scan, parse, compile and load) the expression.
        ee.cook((String) node.getProperty(EXPRESSION));

        return (boolean) ee.evaluate(arguments);
    }

    static RelationshipType trueOrFalse(Node node, Map<String, Object> facts) throws Exception {
        ExpressionEvaluator ee = new ExpressionEvaluator();
        ee.setExpressionType(boolean.class);

        String[] parameterNames = Magic.explode((String) node.getProperty(PARAMETER_NAMES, node.getProperty(ID, EMPTY_STRING)));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) node.getProperty(PARAMETER_TYPES, node.getProperty(TYPE, EMPTY_STRING)));

        // Fill the arguments array with their corresponding values
        Object[] arguments = new Object[parameterNames.length];
        for (int j = 0; j < parameterNames.length; ++j) {
            arguments[j] = Magic.createObject(parameterTypes[j], facts.get(parameterNames[j]).toString());
        }

        // Set our parameters with their matching types
        ee.setParameters(parameterNames, parameterTypes);

        // And now we "cook" (scan, parse, compile and load) the expression.
        ee.cook((String) node.getProperty(EXPRESSION));

        return RelationshipType.withName("IS_" + ee.evaluate(arguments).toString().toUpperCase());
    }

    static RelationshipType choosePath(Node node, Map<String, Object> facts) throws Exception {
        ScriptEvaluator se = new ScriptEvaluator();
        se.setReturnType(String.class);

        // Get the properties of the node stored in the node
        String[] parameterNames = Magic.explode((String) node.getProperty(PARAMETER_NAMES, node.getProperty(ID, EMPTY_STRING)));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) node.getProperty(PARAMETER_TYPES, node.getProperty(TYPE, EMPTY_STRING)));

        // Fill the arguments array with their corresponding values
        Object[] arguments = new Object[parameterNames.length];
        for (int j = 0; j < parameterNames.length; ++j) {
            arguments[j] = Magic.createObject(parameterTypes[j], facts.get(parameterNames[j]).toString());
        }

        // Set our parameters with their matching types
        se.setParameters(parameterNames, parameterTypes);

        // And now we "cook" (scan, parse, compile and load) the script.
        se.cook((String) node.getProperty(SCRIPT));

        return RelationshipType.withName((String) se.evaluate(arguments));
    }
}
