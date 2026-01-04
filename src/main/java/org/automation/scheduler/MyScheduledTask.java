package org.automation.scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

public class MyScheduledTask implements Runnable {

    private ScheduledFuture<?> future;   // handle to cancel the schedule
    private int runCount = 0;

    // setter used by the scheduler class
    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    @Override
    public void run() {
        runCount++;
        System.out.println("Task START at " + LocalDateTime.now());

        try {
            Thread.sleep(3000); // simulate work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Task END   at " + LocalDateTime.now());
        System.out.println("Run count = " + runCount);
        System.out.println("--------------------------");

        // Example condition to cancel after 3 runs
//        if (runCount >= 3) {
//            cancel();
//        }
    }

    public void cancel() {
        if (future != null) {
            System.out.println("Cancelling task at " + LocalDateTime.now());
            future.cancel(false); // false = do not interrupt if running
        }
    }
}

