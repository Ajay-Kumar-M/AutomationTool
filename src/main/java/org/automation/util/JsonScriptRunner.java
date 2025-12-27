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

    public void runFromJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = Files.readString(Paths.get(filePath));

        List<Action> actions = mapper.readValue(jsonContent, new TypeReference<List<Action>>() {});

        for (Action action : actions) {
            System.out.println("Executing: " + action.actionType() + " | TestCase: " + action.testcaseId());
            try {
//                driver.execute(action);
                driver.getClass().getMethod(action.actionType(), Action.class).invoke(driver,action);
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(),action.actionType(),action.locator(),"Success","",new String[0]));
            } catch (InvocationTargetException e) {
                Throwable original = e.getCause();
                original.printStackTrace();
                System.out.println("\nPrinting message " + original.getMessage());
                // handle logging / recovery / reporting
                queueManager.addRecord(new ExpectedResultData(action.testcaseId(),action.actionType(),action.locator(),"Failure",original.getMessage(),new String[0]));
            }
            // Optional: validate expected_result, take screenshot, etc.
        }
    }

    public void run(Driver browser, String filePath) throws Exception {
        this.driver = browser;
        runFromJson(filePath);

        // Automatic flush at 5-min intervals OR
        queueManager.flushNow(); // Manual trigger
        // App shutdown
//        queueManager.shutdown();

    }

    public static void main(String[] args) throws Exception {
        Driver browser = BrowserConfig.getBrowserActions();
//        driver = browser;
        JsonScriptRunner runner = new JsonScriptRunner();
        runner.runFromJson("src/main/java/org/automation/data/TC002.json");

        // Automatic flush at 5-min intervals OR
        queueManager.flushNow(); // Manual trigger
        // App shutdown
        queueManager.shutdown();
        browser.close();
    }
}
