package com.maxdemarzi;

import com.maxdemarzi.decisions.DecisionTree;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.maxdemarzi.schema.Properties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepWiseDecisionTreeArrayTest {

    @Test
    void shouldWork() {

        //"CREATE (number_of_categories_rule:Rule { parameter_names: 'categories', parameter_types:'String[]', script:'switch(categories.size()) { case 0: return \"OPTION_1\"; CASE 1: return \"OPTION_2\"; default: return \"OPTION_3\"; }' })" +
        HashMap<String, Object> node = new HashMap<>();
        node.put(PARAMETER_NAMES, "categories");
        node.put(PARAMETER_TYPES, "String[]");
        node.put(EXPRESSION, "categories.length > 0");

        ArrayList<String> categories = new ArrayList<>();
        categories.add("cat1");

        Map<String, Object> facts = new HashMap<>();
        facts.put("categories", categories.toArray(new String[0]));
        String[] catArray = categories.toArray(new String[0]);
        Class klass = facts.get("categories").getClass();
        String result = null;
        try {
            result = DecisionTree.trueOrFalse(node, facts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //switch(catArray.length) { case 0: return "OPTION_1"; case 1: return "OPTION_2"; default: return "OPTION_3"; }

        assertEquals("IS_TRUE", result);
    }
}
