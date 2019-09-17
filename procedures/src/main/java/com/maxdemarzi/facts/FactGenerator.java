package com.maxdemarzi.facts;

import com.maxdemarzi.results.IntentResult;
import com.maxdemarzi.schema.Labels;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.neo4j.graphdb.*;

import java.time.LocalTime;
import java.util.*;

import static com.maxdemarzi.schema.Properties.NAME;

public class FactGenerator {

    private GraphDatabaseService db;
    private Node account;

    public FactGenerator(GraphDatabaseService db, Node account) {
        this.db = db;
        this.account = account;
    }

    public void getMemberFacts( Map<String, Object> facts) {
        facts.put("account_node_id", account.getId());
        Result factResult = db.execute("MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE ID(a) = $account_node_id RETURN PROPERTIES(member) AS properties LIMIT 1", facts);
        Map<String, Object> factMap = (Map<String, Object>)factResult.next().get("properties");
        facts.putAll(factMap);
        facts.putIfAbsent("name", "");
    }

    public void getTimeFacts(Map<String, Object> facts) {
        facts.put("time_of_day", new TimeOfDay(LocalTime.now()).getTimeOfDay());
    }

    public void getCategoryFacts(IntentResult result, Map<String, Object> facts) {
        HashMap<String, Node> existingCategories = new HashMap<>();

        ResourceIterator<Node> categoryIterator = db.findNodes(Labels.Category);
        while (categoryIterator.hasNext()) {
            Node category = categoryIterator.next();
            existingCategories.put((String)category.getProperty(NAME), category);
        }

        ArrayList<String> categories = new ArrayList<>();
        JaroWinklerDistance distance = new JaroWinklerDistance();
        HashMap<String, Node> matches = new HashMap<>();
        for (Map<String, Object> arg : result.args) {
            if (arg.containsKey("category")) {
                String value = (String)arg.get("category");
                String bestCategory = "";
                double bestValue = 0.50;
                for (String category : existingCategories.keySet()) {
                    double newValue = distance.apply(category, value);
                    if (newValue > bestValue) {
                        bestCategory = category;
                        bestValue = newValue;
                    }
                }

                if (!bestCategory.isEmpty()) {
                    matches.put(value, existingCategories.get(bestCategory));
                    categories.add(bestCategory);
                }
            }
        }

        ArrayList<String> subcategories = new ArrayList<>();
        ArrayList<String> products = new ArrayList<>();

        for (Map.Entry<String, Node> match : matches.entrySet()) {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("category_node_id", match.getValue().getId());

            Result subCategoryResult = db.execute("MATCH (c:Category)<-[:IN_CATEGORY]-(sub:Category) WHERE ID(c) = $category_node_id RETURN sub.name AS name", parameters);
            while (subCategoryResult.hasNext()) {
                subcategories.add((String)subCategoryResult.next().get("name"));
            }

            Result productResult = db.execute("MATCH (c:Category)<-[:IN_CATEGORY]-(sub:Product) WHERE ID(c) = $category_node_id RETURN sub.name AS name", parameters);
            while (productResult.hasNext()) {
                products.add((String)productResult.next().get("name"));
            }
        }

        facts.put("categories", categories.toArray(new String[0]));
        facts.put("subcategories", subcategories.toArray(new String[0]));
        facts.put("products", products.toArray(new String[0]));
    }

}
