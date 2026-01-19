package org.automation.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.automation.executor.TaskManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
public class AppStartupListener implements ServletContextListener {

    private TaskManager taskManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        taskManager = new TaskManager(executor);
        sce.getServletContext().setAttribute("taskManager", taskManager);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        taskManager.shutdown();
    }
}

/*

        System.out.println("==== JVM INPUT ARGUMENTS ====");
        ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .forEach(System.out::println);
 */