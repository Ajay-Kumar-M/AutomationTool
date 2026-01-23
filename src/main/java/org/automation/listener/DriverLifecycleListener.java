package org.automation.listener;

import org.automation.driver.Driver;
import org.automation.driver.DriverConfig;
import org.testng.*;

public class DriverLifecycleListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
//        if (method.isTestMethod()) {
            Driver driver = DriverConfig.getConfigDriver();
            driver.storeInThreadLocal();
//        }
    }

    @Override
    public void onFinish(ISuite suite) {
//        if (method.isTestMethod()) {
            Driver driver = Driver.getFromThreadLocal();
            if (driver != null) {
                driver.close();
            }
            Driver.cleanupThreadLocal();
//        }
    }
}

