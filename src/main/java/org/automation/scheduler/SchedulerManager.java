package org.automation.scheduler;

import org.automation.executor.PlaywrightDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SchedulerManager {
    private ScheduledFuture<?> future;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    public void start(long fixedDelaySeconds, LocalDateTime runAt) {
        long delay = Duration.between(LocalDateTime.now(), runAt).toSeconds();
        MyScheduledTask task = new MyScheduledTask();
        future = scheduler.scheduleAtFixedRate(
                task,
                delay,
                fixedDelaySeconds,
                TimeUnit.SECONDS
        );

        task.setFuture(future);
        logger.info("Schedule Future Task");
        // optional: shutdown executor when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            logger.info("Scheduler shutdown");
        }));
    }

    public void shutdown() {
        if(future!=null){
            future.cancel(false);
        }
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        LocalDateTime runAt = LocalDateTime.of(2026, 1, 1, 15, 34);
        SchedulerManager manager = new SchedulerManager();
        manager.start(60, runAt);
    }
}

