package org.automation.driver;

public class DriverFactory {
    public static Driver createBrowserActions(String tool) {
        if (tool.equalsIgnoreCase("selenium")) {
            return new SeleniumDriver();
        } else if (tool.equalsIgnoreCase("playwright")) {
            return new PlaywrightDriver();
        }
        throw new IllegalArgumentException("Unknown tool: " + tool);
    }
}
