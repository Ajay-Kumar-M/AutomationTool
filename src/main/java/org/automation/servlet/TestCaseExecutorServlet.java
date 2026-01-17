package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.automation.executor.RunTestcase;
import org.automation.executor.TaskManager;
import org.automation.listener.AllureListener;
import org.automation.listener.DriverLifecycleListener;
import org.automation.util.TestUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

@WebServlet("/TestCaseExecutorServlet")
public class TestCaseExecutorServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper responseMapper = new ObjectMapper();
        ObjectNode rootNode = responseMapper.createObjectNode();
        List<String> resolvedFiles = new ArrayList<>();
        try {
            response.setContentType("application/json");
            BufferedReader reader = request.getReader();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode reqBody = (ObjectNode) mapper.readTree(reader);
            List<String> selectedFiles = mapper.convertValue(
                    reqBody.get("selectedFiles"),
                    new TypeReference<List<String>>() {
                    }
            );
            for (String entry : selectedFiles) {
                Path path = Paths.get(entry);
                if (Files.isDirectory(path)) {
                    resolvedFiles.addAll(resolveNestedFiles(path));
                } else if (Files.isRegularFile(path)) {
                    resolvedFiles.add(entry);
                }
            }
            UUID uuid = UUID.randomUUID();
            long lsb = uuid.getLeastSignificantBits() & Long.MAX_VALUE;
            String executionId = Long.toString(lsb, 36);
            TaskManager taskManager = (TaskManager) getServletContext().getAttribute("taskManager");
//            taskManager.submit(executionId, () -> runSelectedTestCases(executionId,resolvedFiles));
            taskManager.submit(executionId, () -> {
                try {
                    String tempFilePath = TestUtils.writeListToTempFile(resolvedFiles,executionId);
                    runSelectedTestCases(executionId, tempFilePath);
                } catch (Throwable t) {
                    System.err.println("Test execution failed for id " + executionId);
                    t.printStackTrace();
                    // optionally update some status in a shared map
                }
            });
            rootNode.put("success", true);
            rootNode.put("executionId", executionId);
            rootNode.put("filesCount", resolvedFiles.size());
            rootNode.put("message", "Testcase execution with ID " + executionId + " has been scheduled !");
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            System.out.println(json);
            response.getWriter().write(json);
        } catch (Exception e) {
            rootNode.put("success", false);
            rootNode.put("filesCount", resolvedFiles.size());
            rootNode.put("message", "Execution failed: " + e.getMessage());
            System.out.println("Exception occurred in TestCaseExecutorServlet" + e.getMessage());
            e.printStackTrace();
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            System.out.println(json);
            response.getWriter().write(json);
        }
    }

    private void runSelectedTestCases(String id,String tempFilePath) {
        try {
            System.out.println("runSelectedTestCases called id:" + id);
            TestNG testng = new TestNG();
            testng.addListener(new AllureListener());
            testng.addListener(new DriverLifecycleListener());
//            testng.setTestClasses(new Class[]{RunTestcase.class});  // Fixed: use .class
            // Create XML suite with parameters
            XmlSuite suite = new XmlSuite();
            suite.setName("TestSuite-" + id);
            XmlTest test = new XmlTest(suite);
            test.setName("Test-" + id);
            // Pass file paths as parameter
            Map<String, String> params = new HashMap<>();
            params.put("testSuiteId", id);
            params.put("tempFilePath", tempFilePath);
            test.setParameters(params);
            test.setXmlClasses(Arrays.asList(new XmlClass(RunTestcase.class)));
            suite.setTests(Arrays.asList(test));
            testng.setXmlSuites(Arrays.asList(suite));
            //testng.setVerbose(10); //testng logging
//            System.out.println("jsonFilePaths param: " + String.join(",", jsonFilePaths));
            testng.run();
        } catch (Exception e) {
            System.out.println("exception occurred in runSelectedTestCases - "+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    List<String> resolveNestedFiles(Path path) throws IOException {
        List<String> filesInDirectory = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    filesInDirectory.addAll(resolveNestedFiles(entry));
                } else if (entry.toString().endsWith(".json")) {
                    filesInDirectory.add(entry.toString());
                }
            }
        }
        return filesInDirectory;
    }
}

/*
//        JsonObject reqBody = JsonParser.parseReader(reader).getAsJsonObject();
//        JsonArray selectedFiles = reqBody.getAsJsonArray("selectedFiles");

//            jsonResponse.addProperty("success", true);
//            jsonResponse.addProperty("executionId", executionId);
//            jsonResponse.addProperty("filesCount", paths.size());
 */

/*

        XmlSuite suite = new XmlSuite();
        suite.setName(id+"_Suite");
        // Optional: suite.setParallel(XmlSuite.ParallelMode.METHODS);
        // suite.setThreadCount(3);

        XmlTest test = new XmlTest(suite);
        test.setName(id+"_Tests");

        List<XmlClass> classes = new ArrayList<>();

        XmlClass xmlClass = new XmlClass(RunTestcase.class);
        // Optional: pass parameters if needed
        // xmlClass.getLocalParameters().put("someKey", "value");

        classes.add(xmlClass);
        test.setXmlClasses(classes);
        JsonFileListHolder.setFiles(jsonFilePaths);

        List<XmlSuite> suites = new ArrayList<>();
        suites.add(suite);

        TestNG tng = new TestNG();
        tng.setXmlSuites(suites);
        // tng.setUseDefaultListeners(false); // if you want to disable default reporters
        tng.run();

        // Optional: clear after run
        JsonFileListHolder.clear();
 */