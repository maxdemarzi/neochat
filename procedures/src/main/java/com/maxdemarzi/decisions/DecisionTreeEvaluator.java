package com.maxdemarzi.decisions;

import com.maxdemarzi.schema.Labels;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.*;

import java.util.Map;

import static com.maxdemarzi.schema.Properties.ID;

public class DecisionTreeEvaluator implements PathEvaluator {
    private Map<String, String> facts;

    public DecisionTreeEvaluator(Map<String, String> facts) {
        this.facts = facts;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState branchState) {
        Node last = path.endNode();

        // If we get to an Answer then stop traversing, we found a valid path.
        if (last.hasLabel(Labels.Answer)) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }

        if(last.hasLabel(Labels.Parameter)) {
            // If we get to a Parameter, check if its missing in our facts, if so we found a valid path.
            if (facts.containsKey(last.getProperty(ID, "").toString())) {
                return Evaluation.EXCLUDE_AND_PRUNE;
            }

            return Evaluation.INCLUDE_AND_PRUNE;
        }

        // If not, continue down this path if there is anything else to find.
        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    @Override
    public Evaluation evaluate(Path path) {
        return null;
    }
}