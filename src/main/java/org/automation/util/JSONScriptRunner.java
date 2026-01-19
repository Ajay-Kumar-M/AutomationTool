package org.automation.util;

import net.sf.jasperreports.engine.JRException;
import org.automation.driver.DriverConfig;
import org.automation.driver.Driver;
import org.automation.records.ActionRecord;
import org.automation.records.ExpectedResultRecord;
import org.automation.records.RecordQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class JSONScriptRunner {
    private Driver driver;
    static RecordQueueManager queueManager = RecordQueueManager.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(JSONScriptRunner.class);

    public void runFromJson(List<ActionRecord> actionRecords) throws Exception {
        for (ActionRecord actionRecord : actionRecords) {
            logger.info("Executing: " + actionRecord.actionType() + " | Method name: " + actionRecord.methodName() +" | TestCase: " + actionRecord.testcaseId());
            try {
                driver.getClass().getMethod(actionRecord.methodName(), ActionRecord.class).invoke(driver, actionRecord);
                queueManager.addRecord(new ExpectedResultRecord(actionRecord.testcaseId(), actionRecord.actionType(), actionRecord.locator(),"Success","",new String[0]));
            } catch (InvocationTargetException | AssertionError e) {
                Throwable original = e.getCause();
                String message = original.getMessage().replace('<',' ').replace('>',' ');
                logger.warn("\nPrinting testrun exception message - " + message);
//                System.out.println("\nPrinting message string -" + original);
                // handle logging / recovery / reporting
                storeResultCSV(actionRecords.getFirst(),"Failure",message.split("\\R", 2)[0]);
                queueManager.addRecord(new ExpectedResultRecord(actionRecord.testcaseId(), actionRecord.actionType(), actionRecord.locator(),"Failure",original.getMessage(),new String[0]));
                // Rethrow the original exception to fail the test
                if (original instanceof AssertionError) {
                    throw (AssertionError) original;
                } else if (original instanceof Error) {
                    throw (Error) original;
                } else {
                    throw new RuntimeException("Test action failed: " + message, original);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                logger.warn("\nPrinting testrun exception - " + e.getMessage());
                logger.warn(Arrays.toString(e.getStackTrace()));
                storeResultCSV(actionRecords.getFirst(),"Failure",e.getMessage());
                queueManager.addRecord(new ExpectedResultRecord(actionRecord.testcaseId(), actionRecord.actionType(), actionRecord.locator(), "Failure", e.getMessage(), new String[0]
                ));
                throw new RuntimeException("Reflection error: " + e.getMessage(), e);
            }
            // Optional: validate expected_result, take screenshot, etc.
        }
        storeResultCSV(actionRecords.getFirst(),"Success","TestCase Executed Successfully");
    }

    public void run(Driver driver, List<ActionRecord> actionRecords) throws Exception {
        this.driver = driver;
        runFromJson(actionRecords);
        queueManager.flushNow();
        // App shutdown
//        queueManager.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Driver driver1 = DriverConfig.getConfigDriver();
        ObjectMapper mapper = new ObjectMapper();
        JSONScriptRunner runner = new JSONScriptRunner();
        try {
            String jsonContent = Files.readString(Paths.get("src/main/java/org/automation/data/TC001.json"));
            List<ActionRecord> actionRecords = mapper.readValue(jsonContent, new TypeReference<List<ActionRecord>>() {});
            runner.run(driver1, actionRecords);
            jsonContent = Files.readString(Paths.get("src/main/java/org/automation/data/TC002.json"));
            actionRecords = mapper.readValue(jsonContent, new TypeReference<List<ActionRecord>>() {});
            runner.run(driver1, actionRecords);

            new File("result").mkdirs();
            TestReportGenerator generator = new TestReportGenerator();
            // Generate all jasper report formats
            generator.generatePdfReport();
//            generator.generateHtmlReport();
            SendReportEmail.sendMail();
            logger.info("\n✓ All reports generated successfully!");
        } catch (JRException | AssertionError e) {
            logger.info("Caught exception JRException | AssertionError : "+e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Automatic flush at 5-min intervals OR
//        queueManager.flushNow(); // Manual trigger
        // App shutdown
        queueManager.shutdown();
        driver1.close();
    }

    private void storeResultCSV(ActionRecord actionRecord, String status, String message){
        File testrunResultFile = new File(TestUtils.getResultDir()+"/automationResult.csv");
        boolean fileExists = testrunResultFile.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testrunResultFile, true))) {
            if (!fileExists) {
                writer.write("TestCaseID,Description,Status,Message");
                writer.newLine();
            }
            writer.write(actionRecord.testcaseId()+","+ actionRecord.description()+","+status+","+message.replace("\"","'"));
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