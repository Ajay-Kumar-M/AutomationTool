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
    private final Driver driver;

    public JsonScriptRunner(Driver driver) {
        this.driver = driver;
    }

    public void runFromJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = Files.readString(Paths.get(filePath));

        List<Action> actions = mapper.readValue(jsonContent, new TypeReference<List<Action>>() {});

        for (Action action : actions) {
            System.out.println("Executing: " + action.action_type() + " | TestCase: " + action.testcase_id());
            try {
//                driver.execute(action);
                driver.getClass().getMethod(action.action_type(), Action.class).invoke(driver,action);
            } catch (InvocationTargetException e) {
                Throwable original = e.getCause();
                original.printStackTrace();
                System.out.println("\nPrinting message " + original.getMessage());
                // handle logging / recovery / reporting
            }
            // Optional: validate expected_result, take screenshot, etc.
        }
    }

    public static void main(String[] args) throws Exception {
        Driver browser = BrowserConfig.getBrowserActions();
        RecordQueueManager queueManager = RecordQueueManager.getInstance();

        JsonScriptRunner runner = new JsonScriptRunner(browser);
        runner.runFromJson("src/main/java/org/automation/data/TC002.json");

        browser.close();
    }
}
