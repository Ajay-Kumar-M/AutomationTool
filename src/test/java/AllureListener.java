import com.microsoft.playwright.Page;
import io.qameta.allure.Attachment;
import org.automation.driver.Driver;
import org.automation.util.ScreenshotManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Map;

/**
 * TestNG Listener for automatic screenshot capture on test failures
 * and detailed Allure reporting
 */
public class AllureListener implements ITestListener {

    @Attachment(value = "Execution Report", type = "text/plain")
    public static String attachExecutionReport(String report) {
        return report;
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("========================================");
        System.out.println("TEST STARTED: " + result.getTestClass()
                .getName() + "." + result.getMethod().getMethodName());
        System.out.println("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            System.out.println("✓ TEST PASSED: " + result.getMethod().getMethodName());
            ITestContext context = result.getTestContext();
            Map<String, Object> state = (Map<String, Object>) context.getAttribute("executionState");
            long duration = System.currentTimeMillis() - (long) state.get("startTime");
            String report = String.format(
                    "Test Passed\nDuration: %dms",
                    duration
            );
            attachExecutionReport(report);
//            String testCaseID = Driver.getTestCaseID();
            String testcaseID = (String) context.getAttribute("testcaseID");
            System.out.println("✓ TESTCaseID: " + testcaseID);
            System.out.println("Screenshot saved to: " + ScreenshotManager.getScreenshotDirectory());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            System.out.println("✗ TEST FAILED: " + result.getMethod().getMethodName());
            ITestContext context = result.getTestContext();
            Map<String, Object> state = (Map<String, Object>) context.getAttribute("executionState");
            long duration = System.currentTimeMillis() - (long) state.get("startTime");
            String report = String.format(
                    "Test Failed\nReason: %s\nDuration: %dms",
                    result.getThrowable().getMessage(),
                    duration
            );
            attachExecutionReport(report);
            String testCaseID = Driver.getTestCaseID();
            // ✓ Retrieve testcaseID and driver from context
            String testcaseID = (String) context.getAttribute("testcaseID");
//            Boolean isWebdriver = (Boolean) context.getAttribute("isWebdriver");
            System.out.println("✗ TESTCaseID: " + testcaseID);
            System.out.println("Screenshot saved to: " + ScreenshotManager.getScreenshotDirectory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⊘ TEST SKIPPED: " + result.getMethod()
                .getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        // Handle soft assertions
        System.out.println("⚠ TEST FAILED (Within success %): " +
                result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n▶ TEST SUITE STARTED: " +
                context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("◀ TEST SUITE FINISHED: " +
                context.getName());
        System.out.println("Total Tests: " +
                context.getAllTestMethods().length);
        System.out.println("Passed: " +
                context.getPassedTests().size());
        System.out.println("Failed: " +
                context.getFailedTests().size());
        System.out.println("Skipped: " +
                context.getSkippedTests().size() + "\n");
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
                System.out.println("Filed name"+field.getName());
                System.out.println("Filed type"+field.getType());
                if (WebDriver.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (WebDriver) field.get(testInstance);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not extract WebDriver: " +
                    e.getMessage());
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
            System.err.println("Could not extract WebDriver: " +
                    e.getMessage());
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