package org.automation.executor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskManager {
    private final ExecutorService executor;
    private final Map<String, Future<?>> runningTasks = new ConcurrentHashMap<>();

    public TaskManager(ExecutorService executor) {
        this.executor = executor;
    }

    public void submit(String id, Runnable task) {
        Future<?> future = executor.submit(() -> {
            try {
                System.out.println("TaskManager submit called "+id);
                task.run();
            } finally {
                runningTasks.remove(id);
            }
        });
        runningTasks.put(id, future);
        System.out.println("TaskManager submit finish called "+id);
    }

    public List<String> getPendingTaskIds() {
        return runningTasks.entrySet().stream()
                .filter(entry -> !entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public String getPendingTasksString(){
        return runningTasks.entrySet().stream()
                .filter(entry -> !entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
    }

    public Set<String> getCompletedTaskIds() {
        return runningTasks.entrySet().stream()
                .filter(entry -> entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Map<String, Boolean> getTaskStatuses() {
        return runningTasks.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> !entry.getValue().isDone()
                ));
    }

    public void shutdown(){
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                // Cancel currently running tasks
                for (Future<?> future : runningTasks.values()) {
                    future.cancel(true); // interrupt if running
                }
                // Force shutdown
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 5. Re-cancel and preserve interrupt status
            for (Future<?> future : runningTasks.values()) {
                future.cancel(true);
            }
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            runningTasks.clear();
        }
    }
}

