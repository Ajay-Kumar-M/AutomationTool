package org.automation.executor;

import org.automation.records.DriverConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DriverFactory {
    private static final Map<String, String> driverClassMap = new HashMap<>();

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config/driver.properties"));

            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("drivers.")) {
                    String driverName = key.substring("drivers.".length());
                    String className = props.getProperty(key);
                    driverClassMap.put(driverName, className);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load drivers config", e);
        }
    }

    public static Driver createBrowserActions(DriverConfig driverConfig) {
        String className = driverClassMap.get(driverConfig.driverType().toLowerCase());

        if (className == null) {
            throw new IllegalArgumentException(
                    "Unknown driver: " + driverConfig.driverType() +
                            ". Available: " + driverClassMap.keySet()
            );
        }

        try {
            Class<?> driverClass = Class.forName(className);
            return (Driver) driverClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create driver: " + driverConfig.driverType(), e);
        }
    }
}

/*
    public static Driver createBrowserActions(DriverConfig driverConfig) {
        if (tool.equalsIgnoreCase("selenium")) {
            return new SeleniumDriver();
        } else if (tool.equalsIgnoreCase("playwright")) {
            return new PlaywrightDriver();
        }
        throw new IllegalArgumentException("Unknown tool: " + tool);
    }
 */