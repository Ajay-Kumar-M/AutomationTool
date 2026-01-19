package org.automation.driver;

import com.microsoft.playwright.Page;
import org.automation.records.ActionRecord;
import org.automation.records.DriverConfigRecord;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Driver {

    static final Logger logger = LoggerFactory.getLogger(Driver.class);
    void init(DriverConfigRecord driverConfigRecord);
    void execute(ActionRecord actionRecord);
    String getText(ActionRecord actionRecord);
    void close();

    default void storeInThreadLocal() {
        DriverThreadLocalManager.setDriver(this);
        logger.info("[DriverManager] Stored {} in ThreadLocal for thread: {}", this.getClass().getSimpleName(), Thread.currentThread().getName());
    }

    static Driver getFromThreadLocal() {
        return DriverThreadLocalManager.getDriver();
    }

    static WebDriver getWebDriverFromThreadLocal() {
        return DriverThreadLocalManager.getWebDriver();
    }

    static Page getPageFromThreadLocal() {
        return DriverThreadLocalManager.getPage();
    }

    static boolean isWebDriver() {
        return DriverThreadLocalManager.isWebDriver();
    }

    static boolean isPlaywright() {
        return !DriverThreadLocalManager.isWebDriver();
    }

    static void setTestCaseID(String testCaseID) {
        DriverThreadLocalManager.setTestCaseID(testCaseID);
    }

    static String getTestCaseID() {
        return DriverThreadLocalManager.getTestCaseID();
    }

    static void cleanupThreadLocal() {
        DriverThreadLocalManager.cleanup();
    }
}

/*

//    void gotoUrl(String url);
//    void type(String locator, String text);
//    void click(String locator);


//    ThreadLocal<WebDriver> driver = new ThreadLocal<>();
//    ThreadLocal<String> testCaseID = new ThreadLocal<>();
//
//    public static void setDriver(WebDriver d) { driver.set(d); }
//    public static WebDriver getDriver() { return driver.get(); }
//
//    public static void setTestCaseID(String id) { testCaseID.set(id); }
//    public static String getTestCaseID() { return testCaseID.get(); }
//
//    public static void cleanup() {
//        driver.remove();
//        testCaseID.remove();
//    }
 */
