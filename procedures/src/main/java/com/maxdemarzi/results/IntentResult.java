package com.maxdemarzi.results;

import java.util.List;
import java.util.Map;

public class IntentResult {
    public final String intent;
    public final List<Map<String, Object>> args;
    public String response = "";

    public IntentResult(String intent, List<Map<String, Object>> args ) {
        this.intent = intent;
        this.args = args;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
