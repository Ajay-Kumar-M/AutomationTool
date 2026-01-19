package org.automation.listener;

import com.microsoft.playwright.Page;
import io.qameta.allure.Attachment;
import org.automation.driver.Driver;
import org.automation.util.ScreenshotManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;

/**
 * TestNG Listener for automatic screenshot capture on test failures
 * and detailed Allure reporting
 */
public class AllureListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(AllureListener.class);

    @Attachment(value = "Execution Report", type = "text/plain")
    public static String attachExecutionReport(String report) {
        return report;
    }

    @Attachment(value = "Testcase ID", type = "text/plain")
    public static String attachTestcaseId(String id) {
        return id;
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("========================================");
        logger.info("TEST STARTED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        logger.info("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            logger.info("✓ TEST PASSED: {}", result.getMethod().getMethodName());
            ITestContext context = result.getTestContext();
            Map<String, Object> state = (Map<String, Object>) context.getAttribute("executionState");
            long duration = System.currentTimeMillis() - (long) state.get("startTime");
            String report = String.format("Test Passed\nDuration: %dms", duration);
            attachExecutionReport(report);
//            String testCaseID = Driver.getTestCaseID();
            String testcaseID = (String) context.getAttribute("testcaseID");
            attachTestcaseId("Testcase ID : "+testcaseID);
            logger.info("✓ TESTCaseID: {}", testcaseID);
            logger.info("Screenshot saved to: {}", ScreenshotManager.getScreenshotDirectory());
        } catch (Exception e) {
            logger.error("onTestSuccess Exception {}", Arrays.toString(e.getStackTrace()));
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            logger.warn("✗ TEST FAILED: {}", result.getMethod().getMethodName());
            ITestContext context = result.getTestContext();
            Map<String, Object> state = (Map<String, Object>) context.getAttribute("executionState");
            long duration = System.currentTimeMillis() - (long) state.get("startTime");
            String report = String.format("Test Failed\nReason: %s\nDuration: %dms",
                    result.getThrowable().getMessage(),
                    duration
            );
            attachExecutionReport(report);
            String testCaseDriverID = Driver.getTestCaseID();
            // ✓ Retrieve testcaseID and driver from context
            String testcaseID = (String) context.getAttribute("testcaseID");
            attachTestcaseId("Testcase ID : "+testcaseID);
//            Boolean isWebdriver = (Boolean) context.getAttribute("isWebdriver");
            logger.warn("✗ TESTCaseID: {}", testcaseID);
            logger.warn("Screenshot saved to: {}", ScreenshotManager.getScreenshotDirectory());
        } catch (Exception e) {
            logger.error("onTestFailure Exception {}", Arrays.toString(e.getStackTrace()));
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.info("⊘ TEST SKIPPED: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        // Handle soft assertions
        logger.warn("⚠ TEST FAILED (Within success %): {}", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("\n▶ TEST SUITE STARTED: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("◀ TEST SUITE FINISHED: {}", context.getName());
        logger.info("Total Tests: {}", context.getAllTestMethods().length);
        logger.info("Passed: {}", context.getPassedTests().size());
        logger.info("Failed: {}", context.getFailedTests().size());
        logger.info("Skipped: {}\n", context.getSkippedTests().size());
    }

    /**
     * Extract WebDriver from test instance (handles different patterns)
     */
    private WebDriver getDriver(ITestResult result) {
        Object testInstance = result.getInstance();
        try {
            // Try to get driver from instance field
            java.lang.reflect.Field[] fields =
                    testInstance.getClass().getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                logger.info("Filed name{}", field.getName());
                logger.info("Filed type{}", field.getType());
                if (WebDriver.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (WebDriver) field.get(testInstance);
                }
            }
        } catch (Exception e) {
            logger.error("Could not extract Selenium WebDriver: {}", e.getMessage());
        }
        return null;
    }

    private Page getPage(ITestResult result) {
        Object testInstance = result.getInstance();
        try {
            // Try to get driver from instance field
            java.lang.reflect.Field[] fields =
                    testInstance.getClass().getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                if (Page.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (Page) field.get(testInstance);
                }
            }
        } catch (Exception e) {
            logger.error("Could not extract Playwright Driver: {}", e.getMessage());
        }
        return null;
    }
}

/*
        if (isWebdriver) {
            WebDriver webDriver = (WebDriver) context.getAttribute("webdriver");
            System.out.println("webdriver: " + webDriver);
//            String screenshotPath = ScreenshotManager.takeScreenshot(
//                    webDriver,
//                    "TestPassed_" + result.getMethod().getMethodName(),
//                    testcaseID
//            );
        } else {
            Page pageDriver = (Page) context.getAttribute("pagedriver");
            System.out.println("pageDriver: " + pageDriver);
            String screenshotPath = ScreenshotManager.takeScreenshot(
                    pageDriver,
                    "TestPassed_" + result.getMethod().getMethodName(),
                    testcaseID
            );
        }



        if (isWebdriver) {
            WebDriver webDriver = (WebDriver) context.getAttribute("webdriver");
            System.out.println("webdriver: " + webDriver);
            String screenshotPath = ScreenshotManager.takeScreenshot(
                    webDriver,
                    "TestFailed_" + result.getMethod().getMethodName(),
                    testcaseID
            );

            // Add failure details to Allure
            if (result.getThrowable() != null) {
                Allure.addAttachment(
                        "Failure Reason",
                        "text/plain",
                        result.getThrowable().getMessage()
                );
            }
        } else {
            Page pageDriver = (Page) context.getAttribute("pagedriver");
            System.out.println("pageDriver: " + pageDriver);
            String screenshotPath = ScreenshotManager.takeScreenshot(
                    pageDriver,
                    "TestFailed_" + result.getMethod().getMethodName(),
                    testcaseID
            );

            // Add failure details to Allure
            if (result.getThrowable() != null) {
                Allure.addAttachment(
                        "Failure Reason",
                        "text/plain",
                        result.getThrowable().getMessage()
                );
            }
        }

 */

/*
// Generate multi-page report
            System.out.println("Generating multi-page Allure report...");
            ProcessBuilder generateReport = new ProcessBuilder(
                    "allure", "generate",
                    "target/allure-results",
                    "--clean",
                    "-o", "target/allure-report"
            );

            Process process1 = generateReport.start();
            int exitCode1 = process1.waitFor();

            if (exitCode1 == 0) {
                System.out.println("✓ Multi-page report generated successfully!");
            } else {
                System.out.println("✗ Failed to generate multi-page report");
            }
 */