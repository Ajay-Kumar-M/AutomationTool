package org.automation.util;

import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.automation.records.Action;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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
            driver.execute(action);
            // Optional: validate expected_result, take screenshot, etc.
        }
    }

    public static void main(String[] args) throws Exception {
        Driver browser = BrowserConfig.getBrowserActions();

        JsonScriptRunner runner = new JsonScriptRunner(browser);
        runner.runFromJson("src/main/java/org/automation/data/testdata.json");

        browser.close();
    }
}
