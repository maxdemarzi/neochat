package com.maxdemarzi.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    HAS,
    HAS_ACTION,
    HAS_OPTION,
    IN_CATEGORY,
    REQUIRES
}