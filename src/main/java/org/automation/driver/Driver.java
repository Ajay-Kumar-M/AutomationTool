package org.automation.driver;

import org.automation.records.Action;
import org.openqa.selenium.WebDriver;

public interface Driver {
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

    void init(String browser);
//    void gotoUrl(String url);
//    void type(String locator, String text);
//    void click(String locator);
    void execute(Action action);
    String getText(Action action);
    void close();
}
