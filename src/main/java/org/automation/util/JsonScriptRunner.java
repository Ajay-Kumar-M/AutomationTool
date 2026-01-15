package org.automation.util;

import net.sf.jasperreports.engine.JRException;
import org.automation.executor.DriverConfig;
import org.automation.executor.Driver;
import org.automation.records.Action;
import org.automation.records.ExpectedResultData;
import org.automation.records.RecordQueueManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.List;

public class JsonScriptRunner {
    private Driver driver;
    static RecordQueueManager queueManager = RecordQueueManager.getInstance();

    public void runFromJson(List<Action> actions) throws Exception {
        for (Action action : actions) {
            System.out.println("Executing: " + action.actionType() + " | TestCase: " + action.testcaseId());
            try {
                driver.getClass().getMethod(action.actionType(), Action.class).invoke(driver,action);
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(),action.actionType(),action.locator(),"Success","",new String[0]));
            } catch (InvocationTargetException | AssertionError e) {
                Throwable original = e.getCause();
                original.printStackTrace();
                System.out.println("\nPrinting message -" + original.getMessage());
                System.out.println("\nPrinting message string -" + original);
                // handle logging / recovery / reporting
                storeResultCSV(actions.getFirst(),"Failure",original.getMessage().split("\\R", 2)[0]);
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
                e.printStackTrace();
                storeResultCSV(actions.getFirst(),"Failure",e.getMessage());
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(), action.actionType(), action.locator(), "Failure", e.getMessage(), new String[0]
                ));
                throw new RuntimeException("Reflection error: " + e.getMessage(), e);
            }
            // Optional: validate expected_result, take screenshot, etc.
        }
        storeResultCSV(actions.getFirst(),"Success","TestCase Executed Successfully");
    }

    public void run(Driver driver, List<Action> actions) throws Exception {
        this.driver = driver;
        runFromJson(actions);
        queueManager.flushNow();
        // App shutdown
//        queueManager.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Driver driver1 = DriverConfig.getConfigDriver();
        ObjectMapper mapper = new ObjectMapper();
        JsonScriptRunner runner = new JsonScriptRunner();
        try {
            String jsonContent = Files.readString(Paths.get("src/main/java/org/automation/data/TC001.json"));
            List<Action> actions = mapper.readValue(jsonContent, new TypeReference<List<Action>>() {});
            runner.run(driver1, actions);
            jsonContent = Files.readString(Paths.get("src/main/java/org/automation/data/TC002.json"));
            actions = mapper.readValue(jsonContent, new TypeReference<List<Action>>() {});
            runner.run(driver1, actions);

            new File("result").mkdirs();
            TestReportGenerator generator = new TestReportGenerator();
            // Generate all jasper report formats
            generator.generatePdfReport();
//            generator.generateHtmlReport();
            SendReportEmail.sendMail();
            System.out.println("\n✓ All reports generated successfully!");
        } catch (JRException | AssertionError e) {
            System.out.println("Caught exception JRException | AssertionError : "+e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Automatic flush at 5-min intervals OR
//        queueManager.flushNow(); // Manual trigger
        // App shutdown
        queueManager.shutdown();
        driver1.close();
    }

    private void storeResultCSV(Action action, String status, String message){
        File file = new File("result/automationResult.csv");
        boolean fileExists = file.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (!fileExists) {
                writer.write("TestCaseID,Description,Status,Message");
                writer.newLine();
            }
            writer.write(action.testcaseId()+","+action.description()+","+status+","+message.replace("\"","'"));
            writer.newLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/*
//    public JsonScriptRunner(Driver driver) {
//        this.driver = driver;
//    }
 */
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