package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.automation.util.TestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
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
            String externalDir = TestUtils.getTempDir();
            File folder = new File(externalDir);
            System.out.println("External folder: " + folder.getAbsolutePath());
            System.out.println("External dir: " + externalDir);
            Path filePath = Paths.get(request.getParameter("filePath"));
            System.out.println("filePath - "+filePath);
            if (Files.exists(filePath)) {
                //String fileContent = Files.readString(filePath); // Java 11+
//                rootNode.put("data", Files.readString(filePath));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonData = mapper.readTree(filePath.toFile());
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
