package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/TestCaseLoadServlet")
public class TestCaseLoadServlet extends HttpServlet {
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper responseMapper = new ObjectMapper();
        ObjectNode rootNode = responseMapper.createObjectNode();
        try {
            Path testcaseFilePath = Paths.get(request.getParameter("filePath"));
            System.out.println("TestCaseLoad Filepath- "+ testcaseFilePath);
            if (Files.exists(testcaseFilePath)) {
                //String fileContent = Files.readString(filePath); // Java 11+
//                rootNode.put("data", Files.readString(filePath));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonData = mapper.readTree(testcaseFilePath.toFile());
                rootNode.set("data", jsonData);
                rootNode.put("success", true);
            } else {
                rootNode.put("success", false);
                rootNode.put("message", "File not found, please check file path.");
            }
        } catch (Exception e) {
            rootNode.put("success", false);
            rootNode.put("message", "Read JSON file failed: " + e.getMessage());
            System.out.println("Exception occurred in TestCaseSaveServlet" + e.getMessage());
            e.printStackTrace();
        } finally {
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//            System.out.println(json);
            response.getWriter().write(json);
        }
    }

}
