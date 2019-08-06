package com.maxdemarzi.results;

import org.neo4j.graphdb.Node;

public class ValidationResult {
    public final Node parameter;
    public final boolean valid;

    public ValidationResult(Node parameter, boolean valid) {
        this.parameter = parameter;
        this.valid = valid;
    }
}