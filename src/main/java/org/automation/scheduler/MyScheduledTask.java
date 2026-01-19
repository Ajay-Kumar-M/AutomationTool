package org.automation.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

public class MyScheduledTask implements Runnable {

    private ScheduledFuture<?> future;   // handle to cancel the schedule
    private int runCount = 0;
    private static final Logger logger = LoggerFactory.getLogger(MyScheduledTask.class);

    // setter used by the scheduler class
    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    @Override
    public void run() {
        runCount++;
        logger.info("Schedule Task START at " + LocalDateTime.now());
        try {
            Thread.sleep(3000); // simulate work
        } catch (InterruptedException e) {
            logger.warn("Schedule task interrupted");
            Thread.currentThread().interrupt();
        }
        logger.info("Schedule Task END   at " + LocalDateTime.now());
        logger.info("Schedule Task Run count = " + runCount);

        // Example condition to cancel after 3 runs
//        if (runCount >= 3) {
//            cancel();
//        }
    }

    public void cancel() {
        if (future != null) {
            logger.info("Cancelling schedule task at " + LocalDateTime.now());
            future.cancel(false); // false = do not interrupt if running
        }
    }
}

