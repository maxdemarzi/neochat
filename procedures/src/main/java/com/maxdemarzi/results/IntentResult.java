package com.maxdemarzi.results;

import java.util.List;
import java.util.Map;

public class IntentResult {
    public final String intent;
    public final List<Map<String, Object>> args;

    public IntentResult(String intent, List<Map<String, Object>> args ) {
        this.intent = intent;
        this.args = args;
    }
}
