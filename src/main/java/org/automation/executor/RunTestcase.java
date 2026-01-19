package org.automation.executor;

import io.qameta.allure.*;
import io.qameta.allure.model.Label;
import net.sf.jasperreports.engine.JRException;
import org.automation.driver.Driver;
import org.automation.listener.AllureListener;
import org.automation.listener.DriverLifecycleListener;
import org.automation.records.ActionRecord;
import org.automation.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

@Listeners({
        AllureListener.class,
        DriverLifecycleListener.class
})
public class RunTestcase {

    JSONScriptRunner runner = new JSONScriptRunner();
    private static final Logger logger = LoggerFactory.getLogger(RunTestcase.class);

    @BeforeMethod
    public void setUp(ITestContext context) {
        Map<String, Object> executionState = new LinkedHashMap<>();
        executionState.put("startTime", System.currentTimeMillis());
        context.setAttribute("executionState", executionState);
//        String testSuiteId = context.getCurrentXmlTest().getParameter("testSuiteId");
//        String threadName = Thread.currentThread().getName();
//        logger.info("[TEST] Setting up for TestSuite ID: " + testSuiteId +" Thread name: " + threadName);
    }

    @DataProvider(name = "jsonFiles")
    public static Object[][] getJsonFiles(ITestContext context) {
        String tempFilePath = context.getCurrentXmlTest().getParameter("tempFilePath");
        if (tempFilePath == null || tempFilePath.isEmpty()) {
            logger.error("No jsonFilePaths parameter found!");
            return new Object[0][1];  // Return empty array
        }
        try {
            List<String> jsonFilePaths = TestUtils.readListFromFile(tempFilePath);
            Object[][] data = new Object[jsonFilePaths.size()][1];
            for (int i = 0; i < jsonFilePaths.size(); i++) {
                data[i][0] = jsonFilePaths.get(i);
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test( priority = 1, dataProvider = "jsonFiles" )
    @Severity(SeverityLevel.CRITICAL)
    public void testngRunner(String jsonPath, ITestContext context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonContent = Files.readString(Paths.get(jsonPath));
            List<ActionRecord> actionRecords = mapper.readValue(jsonContent, new TypeReference<>() {
            });
            String testCaseId = actionRecords.getFirst().testcaseId();
            Driver.setTestCaseID(testCaseId);
            context.setAttribute("testcaseID", testCaseId);
            logger.info("Running {} in path->{}", testCaseId, jsonPath);
            if (actionRecords.getFirst().epic() != null) {
                Allure.label("epic", actionRecords.getFirst().epic());
                Allure.label("feature", actionRecords.getFirst().feature());
                Allure.label("story", actionRecords.getFirst().story());
                Allure.label("Testcase ID", testCaseId);
                Allure.getLifecycle().updateTestCase(testCase -> {
                    testCase.setDescription(actionRecords.getFirst().description());
                    testCase.setTestCaseId(testCaseId);
                    testCase.setTestCaseName(actionRecords.getFirst().story());
                    testCase.setName(testCaseId + "- " + actionRecords.getFirst().feature());
                    testCase.setFullName(actionRecords.getFirst().story());
                    testCase.setStart(System.currentTimeMillis());
                    testCase.getLabels().removeIf(l -> "subSuite".equals(l.getName()));
                    testCase.getLabels().add(new Label().setName("subSuite").setValue("Test Run"));
                });
            }
            Driver driver = Driver.getFromThreadLocal();
            runner.run(driver, actionRecords);
            if (Driver.isWebDriver()) {
                ScreenshotManager.takeScreenshot(Driver.getWebDriverFromThreadLocal(), "TestCompleted_" + testCaseId, testCaseId);
            } else {
                ScreenshotManager.takeScreenshot(Driver.getPageFromThreadLocal(), "TestCompleted_" + testCaseId, testCaseId);
            }
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

//    @AfterMethod(alwaysRun = true)
//    public void close(){
//        Driver driver = Driver.getFromThreadLocal();
//        if (driver != null) {
//            driver.close();
//        }
//        Driver.cleanupThreadLocal();
//    }

    @AfterSuite
    public void tearDown() {
        try {
            new File("result").mkdirs();
            TestReportGenerator generator = new TestReportGenerator();
            generator.generatePdfReport();
            generateAllureReport();
            SendReportEmail.sendMail();
            logger.info("\n✓ All reports generated successfully!");
        } catch (JRException e) {
            logger.error("Caught exception JRException : "+e.getMessage());
        }
    }

    private void generateAllureReport() {
        try {
            // Generate SINGLE-FILE report (this is what you need!)
            logger.info("Generating single-file Allure report...");
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
                logger.info("✓ Single-file report generated successfully!");
                logger.info("📄 Report location: result/allure-report-single/index.html");
            } else {
                logger.error("✗ Failed to generate single-file report");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error generating Allure report: {}", e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
//            e.printStackTrace();
        }
    }

    public void executeJasperTask(Runnable jasperTask) {
        ExecutorService jasperExecutor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = jasperExecutor.submit(jasperTask);
            future.get(30, TimeUnit.SECONDS);  // Timeout protection
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            jasperExecutor.shutdownNow();
            logger.error("Jasper task timed out");
        } finally {
            shutdownExecutorGracefully(jasperExecutor, 5, TimeUnit.SECONDS);
        }
    }

    private void shutdownExecutorGracefully(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        executor.shutdown();  // Prevent new tasks, allow running tasks to complete
        try {
            // Wait for tasks to finish gracefully
            if (!executor.awaitTermination(timeout, timeUnit)) {
                // Force shutdown if graceful timeout exceeded
                executor.shutdownNow();
                // Final wait for interrupted tasks to respond
                executor.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status and force shutdown
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

}


/*

    @DataProvider(name = "dynamicJsonFiles")
    public Object[][] filesProvider() {
        return testSuiteFiles.stream()
                .map(f -> new Object[]{f})
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "dynamicJsonFiles")
    public Iterator<Object[]> dynamicJsonFiles() {
        List<String> files = JsonFileListHolder.getFiles();
        if (files == null) files = List.of();
        return files.stream()
                .map(path -> new Object[]{ new File(path).getName().replace(".json",""), path })
                .iterator();
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
 */
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
                System.out.println("✗ Failed to generate multipage report");
            }
 */