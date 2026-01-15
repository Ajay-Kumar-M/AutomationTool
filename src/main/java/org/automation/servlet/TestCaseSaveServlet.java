package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@WebServlet("/TestCaseSaveServlet")
public class TestCaseSaveServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper responseMapper = new ObjectMapper();
        ObjectNode rootNode = responseMapper.createObjectNode();
        try {
            response.setContentType("application/json");
            BufferedReader reader = request.getReader();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode reqBody = (ObjectNode) mapper.readTree(reader);
            System.out.println("filename to save - "+reqBody.get("fileName"));
            String operation = reqBody.get("operation").asText();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String path = "";
            if("create".equalsIgnoreCase(operation)){
                path = reqBody.get("location").asText()
                        + "/" + reqBody.get("fileName").asText();
                File file = new File(path);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                writer.writeValue(file, reqBody.get("data"));
                System.out.println("Pretty JSON file created!");
            } else if ("edit".equalsIgnoreCase(operation)){
                path = reqBody.get("filePath").asText();
                writer.writeValue(new File(path), reqBody.get("data"));
                System.out.println("Pretty JSON file edited!");
            }

            rootNode.put("success", true);
            rootNode.put("filePath", path);
//            rootNode.put("filesCount", resolvedFiles.size());
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            response.getWriter().write(json);
        } catch (Exception e) {
            rootNode.put("success", false);
            rootNode.put("message", "Save servlet failed: " + e.getMessage());
            System.out.println("Exception occurred in TestCaseSaveServlet" + e.getMessage());
            e.printStackTrace();
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            System.out.println(json);
            response.getWriter().write(json);
        }
    }
}

/*
String runId = UUID.randomUUID().toString();
 */