package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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
            resolvedFiles.forEach(file -> {
                System.out.println("file path - "+file);
            });
            String executionId = UUID.randomUUID().toString();

            rootNode.put("success", true);
            rootNode.put("executionId", executionId);
            rootNode.put("filesCount", resolvedFiles.size());
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