package org.automation.executor;

import org.automation.records.DriverConfigRecord;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DriverFactory {
    private static final Map<String, String> driverClassMap = new HashMap<>();

    public static void loadDriverProperties() {
        Properties properties = new Properties();

        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("driver.properties")) {
            if (is == null) {
                throw new RuntimeException("driver.properties not found on classpath");
            }
            properties.load(is);
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("drivers.")) {
                    String driverName = key.substring("drivers.".length());
                    String className = properties.getProperty(key);
                    driverClassMap.put(driverName, className);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load driver.properties", e);
        }
    }

    public static Driver createDriverInstance(DriverConfigRecord driverConfigRecord) {
        loadDriverProperties();
        String className = driverClassMap.get(driverConfigRecord.driverType().toLowerCase());

        if (className == null) {
            throw new IllegalArgumentException(
                    "Unknown driver: " + driverConfigRecord.driverType() +
                            ". Available: " + driverClassMap.keySet()
            );
        }

        try {
            Class<?> driverClass = Class.forName(className);
            return (Driver) driverClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create driver: " + driverConfigRecord.driverType(), e);
        }
    }
}

/*
    public static Driver createDriverInstance(DriverConfig driverConfig) {
        if (tool.equalsIgnoreCase("selenium")) {
            return new SeleniumDriver();
        } else if (tool.equalsIgnoreCase("playwright")) {
            return new PlaywrightDriver();
        }
        throw new IllegalArgumentException("Unknown tool: " + tool);
    }
 */