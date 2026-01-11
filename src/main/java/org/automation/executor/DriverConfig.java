package org.automation.executor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DriverConfig {
    private static final Properties config = new Properties();
    private static org.automation.records.DriverConfig driverConfig;
    static {
        try {
            config.load(new FileInputStream("config/driver.properties"));
            driverConfig = new org.automation.records.DriverConfig(
                    config.getProperty("browser.type", "chrome"),
                    config.getProperty("browser.implicitWait", "10"),
                    config.getProperty("driver.type", "selenium"),
                    Boolean.valueOf(config.getProperty("docker.enable", "false")),
                    config.getProperty("docker.url", ""),
                    config.getProperty("docker.containerName", "")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Driver getBrowserActions() {
        Driver driver = DriverFactory.createBrowserActions(driverConfig);
        driver.init(driverConfig);
        return driver;
    }
}