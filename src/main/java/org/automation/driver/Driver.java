package org.automation.driver;

import com.microsoft.playwright.Page;
import org.automation.records.Action;
import org.openqa.selenium.WebDriver;

public interface Driver {

    void init(String browser, Boolean isDocker, String dockerUrl, String containerName);
    void execute(Action action);
    String getText(Action action);
    void close();

    default void storeInThreadLocal() {
        DriverThreadLocalManager.setDriver(this);
        System.out.println("[DriverManager] Stored " + this.getClass().getSimpleName() + " in ThreadLocal for thread: " + Thread.currentThread().getName());
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
