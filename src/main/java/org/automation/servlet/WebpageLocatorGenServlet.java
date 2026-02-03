package org.automation.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.automation.util.WebpageLocatorGenerator;
import org.automation.records.LocatorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/WebpageLocatorGenServlet")
public class WebpageLocatorGenServlet extends HttpServlet {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(WebpageLocatorGenServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        WebpageLocatorGenerator generator = new WebpageLocatorGenerator();
        List<LocatorData> records = generator.generateLocators(url);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, records);
        logger.info("Webpage Locators Generated - {}",records.toString());
        out.flush();
    }
}