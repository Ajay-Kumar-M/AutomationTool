package org.automation.util;

import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.automation.records.Action;
import org.automation.records.ExpectedResultData;
import org.automation.records.RecordQueueManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.List;

public class JsonScriptRunner {
    private Driver driver;
    static RecordQueueManager queueManager = RecordQueueManager.getInstance();

//    public JsonScriptRunner(Driver driver) {
//        this.driver = driver;
//    }

    public void runFromJson(List<Action> actions) throws Exception {
        for (Action action : actions) {
            System.out.println("Executing: " + action.actionType() + " | TestCase: " + action.testcaseId());
            try {
                driver.getClass().getMethod(action.actionType(), Action.class).invoke(driver,action);
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(),action.actionType(),action.locator(),"Success","",new String[0]));
            } catch (InvocationTargetException e) {
                Throwable original = e.getCause();
                original.printStackTrace();
                System.out.println("\nPrinting message " + original.getMessage());
                // handle logging / recovery / reporting
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(),action.actionType(),action.locator(),"Failure",original.getMessage(),new String[0]));
                // Rethrow the original exception to fail the test
                if (original instanceof AssertionError) {
                    throw (AssertionError) original;
                } else if (original instanceof Error) {
                    throw (Error) original;
                } else {
                    throw new RuntimeException("Test action failed: " + original.getMessage(), original);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // Handle reflection-specific exceptions
                e.printStackTrace();
                queueManager.addRecord(new ExpectedResultData(
                        action.testcaseId(),
                        action.actionType(),
                        action.locator(),
                        "Failure",
                        e.getMessage(),
                        new String[0]
                ));
                throw new RuntimeException("Reflection error: " + e.getMessage(), e);
            }
            // Optional: validate expected_result, take screenshot, etc.
        }
    }

    public void run(Driver browser, List<Action> actions) throws Exception {
        this.driver = browser;
        runFromJson(actions);
        queueManager.flushNow(); // Manual trigger
        // App shutdown
//        queueManager.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Driver browser = BrowserConfig.getBrowserActions();
//        driver = browser;
        JsonScriptRunner runner = new JsonScriptRunner();
//        runner.runFromJson("src/main/java/org/automation/data/TC002.json");

        // Automatic flush at 5-min intervals OR
        queueManager.flushNow(); // Manual trigger
        // App shutdown
        queueManager.shutdown();
        browser.close();
    }
}

/*

//        ObjectMapper mapper = new ObjectMapper();
//        String jsonContent = Files.readString(Paths.get(filePath));
//        List<Action> actions = mapper.readValue(jsonContent, new TypeReference<List<Action>>() {});
 */

/*
import org.testng.Assert;

try {
    driver.getClass().getMethod(action.actionType(), Action.class).invoke(driver, action);
    queueManager.addRecord(new ExpectedResultData(
        action.testcaseId(),
        action.actionType(),
        action.locator(),
        "Success",
        "",
        new String[0]
    ));
} catch (InvocationTargetException e) {
    Throwable original = e.getCause();
    original.printStackTrace();
    System.out.println("\nPrinting message: " + original.getMessage());

    // Log the failure
    queueManager.addRecord(new ExpectedResultData(
        action.testcaseId(),
        action.actionType(),
        action.locator(),
        "Failure",
        original.getMessage(),
        new String[0]
    ));

    // Fail the test explicitly
    Assert.fail("Test action '" + action.actionType() + "' failed: " + original.getMessage(), original);
} catch (Exception e) {
    e.printStackTrace();
    queueManager.addRecord(new ExpectedResultData(
        action.testcaseId(),
        action.actionType(),
        action.locator(),
        "Failure",
        e.getMessage(),
        new String[0]
    ));
    Assert.fail("Unexpected error during test execution: " + e.getMessage(), e);
}

 */