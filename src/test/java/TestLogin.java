import io.qameta.allure.*;
import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.automation.driver.PlaywrightDriver;
import org.automation.driver.SeleniumDriver;
import org.automation.util.JsonScriptRunner;
import org.automation.util.ScreenshotManager;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Listeners({AllureListener.class})
public class TestLogin {
    JsonScriptRunner runner = new JsonScriptRunner();

    @BeforeMethod
    public void setUp(ITestContext context) {  // ✓ Add parameter
        Map<String, Object> executionState = new LinkedHashMap<>();
        executionState.put("startTime", System.currentTimeMillis());
        context.setAttribute("executionState", executionState);
        String threadName = Thread.currentThread().getName();
        System.out.println("[TEST] Setting up for thread: " + threadName);
        Driver browser = BrowserConfig.getBrowserActions();
        browser.storeInThreadLocal();
    }

    @DataProvider(name = "jsonTestCases", parallel = true)
    public Object[][] jsonTestCases() {
        File folder = new File("src/main/java/org/automation/data");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        Object[][] data = new Object[files.length][2];
        for (int i = 0; i < files.length; i++) {
            String testCaseId = files[i].getName().replace(".json", "");
            data[i][0] = testCaseId;
            data[i][1] = files[i].getPath();
        }
        return data;
    }

    @Test(
            priority = 1,
            dataProvider = "jsonTestCases",
            description = "To verify login"
    )
    @Description("To verify login")
    @Epic("EP001")
    @Feature("Feature1: Login check")
    @Story("Story: login verification")
    @Step("Verify login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginTest(String testCaseId, String jsonPath, ITestContext context) throws Exception {
        Driver.setTestCaseID(testCaseId);
        context.setAttribute("testcaseID", testCaseId);
        System.out.println("Running "+testCaseId+" in path->"+jsonPath);
        Driver browser = Driver.getFromThreadLocal();
        runner.run(browser, jsonPath);
        if (Driver.isWebDriver()) {
            ScreenshotManager.takeScreenshot(
                    Driver.getWebDriverFromThreadLocal(),
                    "TestCompleted_" + testCaseId,
                    testCaseId
            );
        } else {
            ScreenshotManager.takeScreenshot(
                    Driver.getPageFromThreadLocal(),
                    "TestCompleted_" + testCaseId,
                    testCaseId
            );
        }
    }

    @AfterMethod
    public void tearDown(ITestContext context) {
        Driver browser = Driver.getFromThreadLocal();
        if (browser != null) {
            browser.close();
        }
        //clean threadlocal
        Driver.cleanupThreadLocal();
    }
}

/*

//        context.setAttribute("testcaseID", "TC001");
//        runner.run(browser,"src/main/java/org/automation/data/TC001.json");


@Listeners({AllureListener.class})
public class TestLogin {

    @BeforeMethod
    public void setUp(ITestContext context) {
        // Initialize comprehensive execution state
        Map<String, Object> executionState = new LinkedHashMap<>();
        executionState.put("startTime", System.currentTimeMillis());
        executionState.put("stepsExecuted", new ArrayList<>());
        executionState.put("errorPoint", null);
        executionState.put("failureDetails", null);

        context.setAttribute("executionState", executionState);
    }

    @Test
    public void test(ITestContext context) {
        Map<String, Object> state =
            (Map<String, Object>) context.getAttribute("executionState");
        List<String> steps = (List<String>) state.get("stepsExecuted");

        try {
            // Step 1
            driver.navigate().to("...");
            steps.add("Navigated");

            // Step 2
            driver.findElement(By.id("email")).sendKeys("...");
            steps.add("Email entered");

            // Step 3 - FAILURE POINT
            driver.findElement(By.id("submit")).click();
            steps.add("Submit clicked");
        } catch (Exception e) {
            // Record failure state
            state.put("errorPoint", e.getClass().getSimpleName());
            state.put("failureDetails", e.getMessage());
            throw e;
        }
    }
}

@Override
public void onTestFailure(ITestResult result) {
    ITestContext context = result.getTestContext();
    Map<String, Object> state =
        (Map<String, Object>) context.getAttribute("executionState");

    // Create comprehensive report with FINAL execution state
    List<String> steps = (List<String>) state.get("stepsExecuted");
    String errorPoint = (String) state.get("errorPoint");
    String failureDetails = (String) state.get("failureDetails");
    long duration = System.currentTimeMillis() - (long) state.get("startTime");

    String report = String.format(
        "Test Failed\nSteps: %s\nError: %s (%s)\nDuration: %dms",
        String.join(" → ", steps),
        errorPoint,
        failureDetails,
        duration
    );

    Allure.addAttachment("Execution Report", "text/plain", report);
}

 */