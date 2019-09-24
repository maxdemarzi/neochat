package com.maxdemarzi;

import org.neo4j.driver.v1.*;
import org.neo4j.helpers.collection.Iterators;

import java.util.*;

public interface CypherQueries {

    String getPassword = "MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE a.id = $id AND member.phone = $phone RETURN a.password AS password";
    String getMember = "MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE a.id = $id AND member.phone = $phone RETURN member";
    String authorizeMember = "MATCH (a:Account)-[:HAS_MEMBER]->(member:Member) WHERE member.token = $token RETURN a.id AS id, member.phone AS phone";
    String tokenizeMember = "MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE a.id = $id AND member.phone = $phone SET member.token = $phone + toString(rand()) RETURN member.token AS token";
    String createMember = "CREATE (a:Account { id: $id, password: $password })-[:HAS_MEMBER]->(member:Member { phone: $phone } ) RETURN a.id AS id, member.phone AS phone";
    String enrichUser = "MATCH (a:Account)-[:HAS_MEMBER]->(member) WHERE a.id = $id AND member.phone = $phone SET member += $properties RETURN member";
    String chat = "CALL com.maxdemarzi.chat($id, $phone, $text)";

    static List<Map<String, Object>> Chat(Driver driver, String id, String phone, String text) {
        return Iterators.asList(
            query(driver, chat, new HashMap<String, Object>() {{
                put("id", id);
                put("phone", phone);
                put("text", text);
            }})
        );
    }

    static Map<String, Object> EnrichMember(Driver driver, String id, String phone, Map<String, Object> properties) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, enrichUser,
                new HashMap<String, Object>() {{
                    put("id", id);
                    put("phone", phone);
                    put("properties", properties);
            }}
        ));

        if (response != null) {
            return (Map<String, Object>) response.get("member");
        }
        return null;
    }

    static Map<String, Object> CreateMember(Driver driver, String id, String phone, String password) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, createMember,
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

    static Map<String, Object> GetMember(Driver driver, String id, String phone) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, getMember,
                new HashMap<String, Object>() {{
                    put("id", id);
                    put("phone", phone);
        }} ));
        if (response != null) {
            return (Map<String, Object>) response.get("member");
        }
        return null;
    }

    static String GetPassword(Driver driver, String id, String phone) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, getPassword,
                new HashMap<String, Object>() {{
                    put("id", id);
                    put("phone", phone);
                }} ));
        if (response != null) {
            return (String) response.get("password");
        }
        return null;
    }

    static Map<String, Object> AuthorizeMember(Driver driver, String token) {
        return Iterators.singleOrNull(query(driver, authorizeMember, new HashMap<String, Object>() {{ put("token", token); }} ));
    }

    static String TokenizeMember(Driver driver, String id, String phone) {
        Map<String, Object> response = Iterators.singleOrNull(query(driver, tokenizeMember,
                new HashMap<String, Object>() {{
                    put("id", id);
                    put("phone", phone);
                }} ));
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
