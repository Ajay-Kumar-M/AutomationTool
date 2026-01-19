package org.automation.driver;

import org.automation.records.DriverConfigRecord;

import java.io.InputStream;
import java.util.Properties;

public class DriverConfig {
    private static final Properties config = new Properties();
    private static final DriverConfigRecord driverConfigRecord;

    static {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("driver.properties")) {
            if (is == null) {
                throw new RuntimeException("driver.properties not found on classpath");
            }
            config.load(is);
            driverConfigRecord = new DriverConfigRecord(
                    config.getProperty("browser.type", "chrome"),
                    config.getProperty("browser.implicitWait", "10"),
                    config.getProperty("driver.type", "selenium"),
                    Boolean.valueOf(config.getProperty("docker.enable", "false")),
                    config.getProperty("docker.url", ""),
                    config.getProperty("docker.containerName", "")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load driver.properties", e);
        }
    }

    public static Driver getConfigDriver() {
        Driver driver = DriverFactory.createDriverInstance(driverConfigRecord);
        driver.init(driverConfigRecord);
        return driver;
    }
}