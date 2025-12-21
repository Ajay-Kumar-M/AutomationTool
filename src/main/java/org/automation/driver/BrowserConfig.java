package org.automation.driver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BrowserConfig {
    private static final Properties config = new Properties();

    static {
        try {
            config.load(new FileInputStream("browser.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Driver getBrowserActions() {
        String tool = config.getProperty("browser.tool", "selenium");
        String browserName = config.getProperty("browser.name", "chrome");

        Driver actions = DriverFactory.createBrowserActions(tool);
        actions.init(browserName);
        return actions;
    }
}
