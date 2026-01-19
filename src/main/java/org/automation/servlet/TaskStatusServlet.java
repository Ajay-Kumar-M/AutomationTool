package org.automation.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.automation.executor.TaskManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/TaskStatusServlet")
public class TaskStatusServlet extends HttpServlet {

//    private static final Map<String, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TaskManager taskManager = (TaskManager) getServletContext().getAttribute("taskManager");
        Map<String, Boolean> taskStatuses = taskManager.getTaskStatuses();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, taskStatuses);
        out.flush();
    }
}

