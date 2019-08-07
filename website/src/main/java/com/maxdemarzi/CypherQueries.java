package com.maxdemarzi;

import org.neo4j.driver.v1.*;
import org.neo4j.helpers.collection.Iterators;

import java.util.*;

public interface CypherQueries {

    final String getUser = "MATCH (u:User) WHERE u.username = {username} RETURN u";
    final String authorizeUser = "MATCH (u:User) WHERE u.token = {token} RETURN u";
    final String tokenizeUser = "MATCH (u:User) WHERE u.username = {username} SET u.token = u.username + toString(rand()) RETURN u.token AS token";
    final String createUser = "CREATE (u:User {username: {username}, email: {email}, password: {password} }) RETURN u";

    static Map<String, Object> CreateUser(Driver driver, String username, String email, String password) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, createUser,
                new HashMap<String, Object>() {{
                    put("username", username);
                    put("email", email);
                    put("password", password); }}
            ));

        if (response != null) {
            return (Map<String, Object>) response.get("u");
        }
        return null;
    }

    static Map<String, Object> GetUser(Driver driver, String username) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, getUser, new HashMap<String, Object>() {{ put("username", username); }} ));
        if (response != null) {
            return (Map<String, Object>) response.get("u");
        }
        return null;
    }

    static Map<String, Object> AuthorizeUser(Driver driver, String token) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, authorizeUser, new HashMap<String, Object>() {{ put("token", token); }} ));
        if (response != null) {
            return (Map<String, Object>) response.get("u");
        }
        return null;
    }

    static String TokenizeUser(Driver driver, String username) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, tokenizeUser, new HashMap<String, Object>() {{ put("username", username); }} ));
        return response.getOrDefault("token", null).toString();
    }

    static Iterator<Map<String, Object>> query(Driver driver, String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            List<Map<String, Object>> list = session.run(query, params)
                    .list( r -> r.asMap(CypherQueries::convert));
            return list.iterator();
        }
    }

    static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(CypherQueries::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }
        return value.asObject();
    }
}
