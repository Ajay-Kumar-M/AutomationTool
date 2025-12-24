package org.automation.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.automation.records.ExpectedResultData;


/**
 * Thread-safe queue manager for Record objects with automatic and manual JSON persistence.
 *
 * Features:
 * - Package-scoped singleton access via getInstance()
 * - Automatic flushing every 5 minutes using ScheduledExecutorService
 * - Manual flush trigger via flushNow()
 * - Thread-safe queue operations using ConcurrentLinkedQueue
 * - File I/O performed asynchronously on separate thread pool
 * - Dynamic filename based on ID extracted from record data
 * - Append mode: new records are appended to existing JSON files
 *
 * Usage:
 *   RecordQueueManager queueManager = RecordQueueManager.getInstance();
 *   queueManager.setRecordIdExtractor(record -> record.getId());
 *   queueManager.addRecord(new Record(...));
 *   queueManager.flushNow(); // manual trigger
 *
 * //@param <> Record type stored in the queue
 */
public class RecordQueueManager {
    private static final Logger logger = LoggerFactory.getLogger(RecordQueueManager.class);
    private static final int SCHEDULED_FLUSH_INTERVAL_MINUTES = 3;
    private static final String DEFAULT_OUTPUT_DIR = "./result";
    private static final String FILE_EXTENSION = ".json";

    private static RecordQueueManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    private final ConcurrentLinkedQueue<ExpectedResultData> queue;
    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService fileWriteExecutor;
    private final ReadWriteLock flushLock;
    private final ObjectMapper objectMapper;
    private final Path outputPath;
    private volatile boolean isShutdown;
    private volatile RecordIdExtractor<ExpectedResultData> recordIdExtractor;

    /**
     * Functional interface to extract ID from record for filename generation.
     */
    @FunctionalInterface
    public interface RecordIdExtractor<ExpectedResultData> {
        String extractId(ExpectedResultData record);
    }

    /**
     * Package-scoped singleton accessor. Thread-safe initialization with double-checked locking.
     */
    public static <ExpectedResultData> RecordQueueManager getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new RecordQueueManager(DEFAULT_OUTPUT_DIR);
                }
            }
        }
        @SuppressWarnings("unchecked")
        RecordQueueManager castedInstance = (RecordQueueManager) instance;
        return castedInstance;
    }

    /**
     * Alternative constructor for custom output directory.
     * Use getInstance() for standard initialization.
     */
    public RecordQueueManager(String outputDirectory) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.flushLock = new ReentrantReadWriteLock();
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.outputPath = Paths.get(outputDirectory);
        this.isShutdown = false;
        this.recordIdExtractor = null;

        // Initialize output directory
        initializeOutputDirectory();

        // Initialize scheduled executor with single thread for periodic flushes
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(
                1,
                runnable -> {
                    Thread thread = new Thread(runnable, "RecordQueueFlushThread");
                    thread.setDaemon(false);
                    return thread;
                }
        );

        // Initialize file write executor for async I/O operations
        this.fileWriteExecutor = Executors.newFixedThreadPool(
                2,
                runnable -> {
                    Thread thread = new Thread(runnable, "RecordQueueFileWriterThread");
                    thread.setDaemon(false);
                    return thread;
                }
        );

        // Schedule periodic flush task
        schedulePeriodicFlush();

        logger.info("RecordQueueManager initialized with output directory: {}", outputDirectory);
    }

    /**
     * Set the function that extracts ID from record data for filename generation.
     * MUST be called before adding records.
     *
     * Example:
     *   queueManager.setRecordIdExtractor(record -> record.getUserId());
     */
//    public void setRecordIdExtractor(RecordIdExtractor<ExpectedResultData> extractor) {
//        this.recordIdExtractor = extractor;
//        logger.info("RecordIdExtractor configured");
//    }

    /**
     * Add a record to the queue. Thread-safe operation.
     */
    public void addRecord(ExpectedResultData record) {
        if (isShutdown) {
            throw new IllegalStateException("RecordQueueManager is shut down");
        }
        if (recordIdExtractor == null) {
            throw new IllegalStateException("RecordIdExtractor must be set before adding records");
        }
        queue.offer(record);
        logger.debug("Record added to queue. Queue size: {}", queue.size());
    }

    /**
     * Manually trigger queue flush to JSON file.
     * File I/O is performed asynchronously on separate thread.
     * Safe to call from any thread.
     */
    public void flushNow() {
        if (isShutdown) {
            logger.warn("Attempted to flush after shutdown");
            return;
        }
        performFlush();
    }

    /**
     * Get current queue size (snapshot).
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Graceful shutdown - flushes remaining records and stops all executors.
     * Waits for file I/O to complete before returning.
     * Call this during application shutdown.
     */
    public void shutdown() {
        synchronized (INSTANCE_LOCK) {
            if (isShutdown) {
                return;
            }
            isShutdown = true;

            // Final flush before shutdown
            if (!queue.isEmpty()) {
                performFlush();
            }

            // Shutdown file write executor (wait for pending writes)
            fileWriteExecutor.shutdown();
            try {
                if (!fileWriteExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    fileWriteExecutor.shutdownNow();
                    logger.warn("File writer executor forced shutdown due to timeout");
                }
            } catch (InterruptedException e) {
                fileWriteExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                logger.error("Interrupted during file writer shutdown", e);
            }

            // Shutdown scheduled executor
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                    logger.warn("Scheduler forced shutdown due to timeout");
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                logger.error("Interrupted during scheduler shutdown", e);
            }
            logger.info("RecordQueueManager shutdown complete");
        }
    }

    // ============== PRIVATE METHODS ==============

    private void initializeOutputDirectory() {
        try {
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                logger.info("Created output directory: {}", outputPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create output directory: {}", outputPath, e);
            throw new RuntimeException("Cannot initialize output directory", e);
        }
    }

    private void schedulePeriodicFlush() {
        scheduledExecutor.scheduleAtFixedRate(
                this::performFlush,
                SCHEDULED_FLUSH_INTERVAL_MINUTES,
                SCHEDULED_FLUSH_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
        logger.info("Scheduled periodic flush every {} minutes", SCHEDULED_FLUSH_INTERVAL_MINUTES);
    }

    private void performFlush() {
        flushLock.writeLock().lock();
        try {
            if (queue.isEmpty()) {
                logger.debug("Queue is empty, skipping flush");
                return;
            }

            // Drain queue into list while holding write lock
//            List<ExpectedResultData> recordsToWrite = new ArrayList<>();
//            ExpectedResultData record;
//            while ((record = queue.poll()) != null) {
//                recordsToWrite.add(record);
//            }

            // Submit async file write task
            fileWriteExecutor.submit(() -> writeRecordsToFile());
            logger.info("Submitted {} records to async file writer", queue.size());
            queue.clear();

        } catch (Exception e) {
            logger.error("Error during queue flush", e);
        } finally {
            flushLock.writeLock().unlock();
        }
    }

    private void writeRecordsToFile() {
        try {
//            for (ExpectedResultData record : records) {
//                // Extract ID from record for filename
//                String recordId = recordIdExtractor.extractId(record);
//                if (recordId == null || recordId.isEmpty()) {
//                    logger.warn("Record ID extractor returned null or empty ID, skipping record");
//                    continue;
//                }
//
//                String filename = recordId + FILE_EXTENSION;
//                Path filePath = outputPath.resolve(filename);
//
//                // Read existing content if file exists
//                List<ExpectedResultData> allRecords = new ArrayList<>();
//                if (Files.exists(filePath)) {
//                    try {
//                        String existingContent = Files.readString(filePath);
//                        @SuppressWarnings("unchecked")
//                        List<ExpectedResultData> existingRecords = objectMapper.readValue(
//                                existingContent,
//                                objectMapper.getTypeFactory().constructCollectionType(List.class, record.getClass())
//                        );
//                        allRecords.addAll(existingRecords);
//                        logger.debug("Read {} existing records from file: {}", existingRecords.size(), filename);
//                    } catch (IOException e) {
//                        logger.warn("Failed to read existing records from {}, will overwrite", filename, e);
//                    }
//                }
//
//                // Add new record to the list
//                allRecords.add(record);
//
//                // Write combined content back to file
//                String jsonContent = objectMapper.writeValueAsString(allRecords);
//                Files.writeString(
//                        filePath,
//                        jsonContent,
//                        StandardOpenOption.CREATE,
//                        StandardOpenOption.WRITE,
//                        StandardOpenOption.APPEND
//                );
//
//                logger.info("Successfully wrote {} total records to file: {}", allRecords.size(), filename);
//            }
            ExpectedResultData record = queue.peek();
            assert record != null;
            String recordId = record.testcaseId(); // recordIdExtractor.extractId(record);
            if (recordId == null || recordId.isEmpty()) {
                logger.warn("Record ID extractor returned null or empty ID, skipping record");
                return;
            }

            String filename = recordId + FILE_EXTENSION;
            Path filePath = outputPath.resolve(filename);
            String jsonContent = objectMapper.writeValueAsString(queue);
            System.out.println(jsonContent);
            Files.writeString(
                    filePath,
                    jsonContent,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
            logger.info("Successfully wrote {} total records to file: {}", queue.size(), filename);
        } catch (Exception e) {
            logger.error("Error writing records to file", e);
        }
    }
}
