package org.automation.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.automation.driver.ActionRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@WebServlet("/TestCaseRecordServlet")
public class TestCaseRecordServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseRecordServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper responseMapper = new ObjectMapper();
        ObjectNode rootNode = responseMapper.createObjectNode();
        try {
            response.setContentType("application/json");
            BufferedReader reader = request.getReader();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode reqBody = (ObjectNode) mapper.readTree(reader);
            String webpageUrl = reqBody.get("webpageUrl").asString();
            String fileName = reqBody.get("fileName").asString();
            String location = reqBody.get("location").asString();
            String testcaseId = reqBody.get("testcaseId").asString();
            String epic = reqBody.get("epic").asString();
            String feature = reqBody.get("feature").asString();
            String story = reqBody.get("story").asString();
            String description = reqBody.get("description").asString();

            new ActionRecorder().recordSession(
                webpageUrl, location+"/"+fileName, testcaseId, epic, feature, story, description
            );

            rootNode.put("success", true);
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            response.getWriter().write(json);
        } catch (Exception e) {
            rootNode.put("success", false);
            rootNode.put("message", "Record servlet failed: " + e.getMessage());
            logger.error("Exception occurred in TestCaseRecordServlet{}", e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            String json = responseMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//            System.out.println(json);
            response.getWriter().write(json);
        }
    }
}