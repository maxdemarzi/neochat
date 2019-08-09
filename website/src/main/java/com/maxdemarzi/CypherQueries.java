package com.maxdemarzi;

import org.neo4j.driver.v1.*;
import org.neo4j.helpers.collection.Iterators;

import java.util.*;

public interface CypherQueries {

    final String getUser = "MATCH (u:Account) WHERE u.id = $id RETURN u";
    final String authorizeUser = "MATCH (u:Account) WHERE u.token = $token RETURN u";
    final String tokenizeUser = "MATCH (u:Account) WHERE u.id = $id SET u.token = u.id + toString(rand()) RETURN u.token AS token";
    final String createUser = "CREATE (u:Account { id: $id, password: $password })-[:HAS_MEMBER]->(member:Member { phone: $phone } ) RETURN u.id AS id, member.phone AS phone";
    final String enrichUser = "MATCH (u:Account)-[:HAS_MEMBER]->(member) WHERE u.id = $id AND member.phone = $phone SET member += $properties RETURN member";
    final String chat = "CALL com.maxdemarzi.chat($id, $text)";

    static List<Map<String, Object>> Chat(Driver driver, String id, String text) {
        return Iterators.asList(
            query(driver, chat, new HashMap<String, Object>() {{
                put("id", id);
                put("text", text);
            }})
        );
    }

    static Map<String, Object> EnrichUser(Driver driver, String email, String phone, Map<String, Object> properties) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, enrichUser,
                new HashMap<String, Object>() {{
                    put("id", email);
                    put("phone", phone);
                    put("properties", properties);
            }}
        ));

        if (response != null) {
            return (Map<String, Object>) response.get("member");
        }
        return null;
    }

    static Map<String, Object> CreateUser(Driver driver, String id, String password, String phone) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, createUser,
                new HashMap<String, Object>() {{
                    put("id", id);
                    put("phone", phone);
                    put("password", password); }}
            ));

        if (response != null) {
            HashMap<String, Object> result = new HashMap<>();
            result.put("id", response.get("id"));
            result.put("phone", response.get("phone"));

            return result;
        }
        return null;
    }

    static Map<String, Object> GetUser(Driver driver, String id) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, getUser, new HashMap<String, Object>() {{ put("id", id); }} ));
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

    static String TokenizeUser(Driver driver, String id) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, tokenizeUser, new HashMap<String, Object>() {{ put("id", id); }} ));
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
