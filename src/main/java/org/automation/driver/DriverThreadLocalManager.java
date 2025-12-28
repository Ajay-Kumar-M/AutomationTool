package org.automation.driver;

import com.microsoft.playwright.Page;
import org.openqa.selenium.WebDriver;

/**
 * Internal ThreadLocal storage manager
 * Hidden from tests - accessed only via Driver interface default methods
 */
class DriverThreadLocalManager {

    private static final ThreadLocal<Driver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isWebDriverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> testCaseIDThreadLocal = new ThreadLocal<>();

    // Package-private methods (not public)

    static void setDriver(Driver driver) {
        if (driver != null) {
            driverThreadLocal.set(driver);
            // Also set the driver type flag
            if (driver instanceof SeleniumDriver) {
                isWebDriverThreadLocal.set(true);
            } else if (driver instanceof PlaywrightDriver) {
                isWebDriverThreadLocal.set(false);
            }
            System.out.println("[DriverManager] Set " + driver.getClass().getSimpleName() + " for thread: " + Thread.currentThread().getName());
        }
    }

    static Driver getDriver() {
        Driver driver = driverThreadLocal.get();
        if (driver == null) {
            System.err.println("[DriverManager] WARNING: Driver is NULL for thread: " +
                    Thread.currentThread().getName());
        }
        return driver;
    }

    static WebDriver getWebDriver() {
        Driver driver = getDriver();
        if (driver != null && driver instanceof SeleniumDriver) {
            return ((SeleniumDriver) driver).getDriver();
        }
        return null;
    }

    static Page getPage() {
        Driver driver = getDriver();
        if (driver != null && driver instanceof PlaywrightDriver) {
            return ((PlaywrightDriver) driver).getPage();
        }
        return null;
    }

    static boolean isWebDriver() {
        Boolean isWebDriver = isWebDriverThreadLocal.get();
        return isWebDriver != null && isWebDriver;
    }

    static void setTestCaseID(String testCaseID) {
        testCaseIDThreadLocal.set(testCaseID);
    }

    static String getTestCaseID() {
        String testCaseID = testCaseIDThreadLocal.get();
        return testCaseID != null ? testCaseID : "UNKNOWN";
    }

    static void cleanup() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[DriverManager] Cleaning up ThreadLocal for thread: " + threadName);

        driverThreadLocal.remove();
        isWebDriverThreadLocal.remove();
        testCaseIDThreadLocal.remove();
    }
}

