package org.automation.others;

import org.automation.driver.Driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestCaseParser {

    public static List<TestStep> parseTestCase(String filePath) throws IOException {
        List<TestStep> steps = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                // Parse: methodName(arg1, arg2, ...)
                TestStep step = parseStep(line);
                steps.add(step);
            }
        }

        return steps;
    }

    private static TestStep parseStep(String line) {
        String methodName = line.substring(0, line.indexOf('('));
        String argsString = line.substring(line.indexOf('(') + 1,
                line.lastIndexOf(')'));
        List<String> args = Arrays.asList(argsString.split(",\\s*"));

        // Remove quotes from arguments
        args = args.stream()
                .map(arg -> arg.replaceAll("^['\"]|['\"]$", ""))
                .collect(Collectors.toList());

        return new TestStep(methodName, args);
    }
}

// Simple POJO for test steps
class TestStep {
    public String methodName;
    public List<String> arguments;

    public TestStep(String methodName, List<String> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }
}


/*
// Execute test case
public static void executeTestCase(Driver browser, String filePath) throws Exception {
    List<TestStep> steps = TestCaseParser.parseTestCase(filePath);

    for (TestStep step : steps) {
        System.out.println("Executing: " + step.methodName);

        Method method = Driver.class.getMethod(step.methodName,
                String.class, String.class); // Adjust for your method signatures

        method.invoke(browser, step.arguments.toArray());
    }
}
 */