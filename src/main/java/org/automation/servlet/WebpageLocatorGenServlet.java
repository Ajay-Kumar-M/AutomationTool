package org.automation.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.automation.WebpageLocatorGenerator;
import org.automation.records.LocatorData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/WebpageLocatorGenServlet")
public class WebpageLocatorGenServlet extends HttpServlet {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        WebpageLocatorGenerator generator = new WebpageLocatorGenerator();
        List<LocatorData> records = generator.generateLocators(url);
//        String json = objectMapper.writeValueAsString(records);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, records);
        System.out.println("Java json --------- \n"+records.toString());
        out.flush();
    }
}