package org.automation.listener;

import org.automation.driver.Driver;
import org.automation.driver.DriverConfig;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class DriverLifecycleListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod()) {
            Driver driver = DriverConfig.getConfigDriver();
            driver.storeInThreadLocal();
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod()) {
            Driver driver = Driver.getFromThreadLocal();
            if (driver != null) {
                driver.close();
            }
            Driver.cleanupThreadLocal();
        }
    }
}

