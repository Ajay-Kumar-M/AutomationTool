import io.qameta.allure.*;
import net.sf.jasperreports.engine.JRException;
import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.automation.records.Action;
import org.automation.util.JsonScriptRunner;
import org.automation.util.ScreenshotManager;
import org.automation.util.SendEmailExample;
import org.automation.util.TestReportGenerator;
import org.testng.ITestContext;
import org.testng.annotations.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Listeners({AllureListener.class})
public class TestLogin {

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

    @DataProvider(name = "jsonTestCases", parallel = false)
    public Object[][] jsonTestCases() {
        File folder = new File("src/main/java/org/automation/data");
        File[] files = folder.listFiles((_, name) -> name.endsWith(".json"));
        if(files!=null){
            Object[][] data = new Object[files.length][2];
            for (int i = 0; i < files.length; i++) {
                String testCaseId = files[i].getName().replace(".json", "");
                data[i][0] = testCaseId;
                data[i][1] = files[i].getPath();
            }
            return data;
        }
        return new Object[0][0];
    }

    @Test(
            priority = 1,
            dataProvider = "jsonTestCases"
    )
    @Severity(SeverityLevel.CRITICAL)
    public void loginTest(String testCaseId, String jsonPath, ITestContext context) throws Exception {
        Driver.setTestCaseID(testCaseId);
        context.setAttribute("testcaseID", testCaseId);
        System.out.println("Running "+testCaseId+" in path->"+jsonPath);
        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = Files.readString(Paths.get(jsonPath));
        List<Action> actions = mapper.readValue(jsonContent, new TypeReference<>() {});
        if(actions.getFirst().epic() != null){
            Allure.label("epic", actions.getFirst().epic());
            Allure.label("feature", actions.getFirst().feature());
            Allure.label("story", actions.getFirst().story());
            Allure.getLifecycle().updateTestCase(testCase -> {
                testCase.setDescription(actions.getFirst().description());
                testCase.setTestCaseId(testCaseId);
            });
        }
        Driver browser = Driver.getFromThreadLocal();
        JsonScriptRunner runner = new JsonScriptRunner();
        runner.run(browser, actions);
        if (Driver.isWebDriver()) {
            ScreenshotManager.takeScreenshot(Driver.getWebDriverFromThreadLocal(), "TestCompleted_" + testCaseId, testCaseId);
        } else {
            ScreenshotManager.takeScreenshot(Driver.getPageFromThreadLocal(), "TestCompleted_" + testCaseId, testCaseId);
        }
    }

    @AfterMethod
    public void close(){
        Driver browser = Driver.getFromThreadLocal();
        if (browser != null) {
            browser.close();
        }
        Driver.cleanupThreadLocal();
    }

    @AfterSuite
    public void tearDown() {
        try {
            System.out.println("\n✓ Tear down triggered!");
            new File("result").mkdirs();
            TestReportGenerator generator = new TestReportGenerator();
            generator.generatePdfReport();
//            generator.generateHtmlReport();
            generateAllureReport();
            SendEmailExample.sendMail("");
            System.out.println("\n✓ All reports generated successfully!");
        } catch (JRException e) {
            System.out.println("Caught exception JRException : "+e.getMessage());
        }
    }

    private void generateAllureReport() {
        try {
            // Generate SINGLE-FILE report (this is what you need!)
            System.out.println("Generating single-file Allure report...");
            ProcessBuilder singleFileReport = new ProcessBuilder(
                    "allure", "generate",
                    "allure-results",
                    "--single-file",
                    "--clean",
                    "-o", "result/allure-report-single"
            );
            Process process2 = singleFileReport.start();
            int exitCode2 = process2.waitFor();
            if (exitCode2 == 0) {
                System.out.println("✓ Single-file report generated successfully!");
                System.out.println("📄 Report location: result/allure-report-single/index.html");
            } else {
                System.out.println("✗ Failed to generate single-file report");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error generating Allure report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/*
//    @Description("To verify login")
//    @Epic("EP001")
//    @Feature("Feature1: Login check")
//    @Story("Story: login verification")
 */
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
            // Record failure state            .put("errorPoint", e.getClass().getSimpleName());
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