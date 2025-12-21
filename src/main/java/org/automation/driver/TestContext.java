package org.automation.driver;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private final Driver driver;
    private final Map<String, String> variables = new HashMap<>();

    public TestContext(Driver driver) {
        this.driver = driver;
    }

    public Driver driver() { return driver; }
    public Map<String, String> variables() { return variables; }

    public void setVar(String key, String value) {
        variables.put(key, value);
    }

    public String getVar(String key) {
        return variables.get(key);
    }
}
