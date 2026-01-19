package org.automation.util;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenshotManager {

    private static final String SCREENSHOT_DIR = "result";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd_HH-mm-ss-SSS";
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotManager.class);

    static {
        createScreenshotDirectory();
    }

    /**
     * Create screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get timestamp for unique file names
     */
    private static String getTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_PATTERN).format(new Date());
    }

    /**
     * Generate unique screenshot filename
     */
    private static String generateFileName(String stepName) {
        String sanitized = stepName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitized + "_" + getTimestamp() + ".png";
    }

    // ========== SELENIUM SCREENSHOTS ==========

    /**
     * Take screenshot for Selenium WebDriver
     * @param driver WebDriver instance
     * @param stepName Description of the action
     * @return Path to saved screenshot
     */
    public static String takeScreenshot(WebDriver driver, String stepName, String testcaseId) {
        if (driver == null) {
            logger.error("WebDriver is null. Cannot capture screenshot.");
            return null;
        }

        try {
            String fileName = generateFileName(stepName);
            String subDir = SCREENSHOT_DIR + File.separator + testcaseId;
            String filePath = SCREENSHOT_DIR + File.separator + testcaseId + File.separator + fileName;

            // Capture screenshot as bytes
            byte[] screenshotBytes = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
            Files.createDirectories(Paths.get(subDir));
            // Save to file
            Files.write(Paths.get(filePath), screenshotBytes);

            // Attach to Allure
            attachToAllure(screenshotBytes, stepName);
            logger.info("[SCREENSHOT] {} -> {}", stepName, filePath);
            return filePath;

        } catch (Exception e) {
            logger.error("Error capturing screenshot: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // ========== PLAYWRIGHT SCREENSHOTS ==========

    /**
     * Take screenshot for Playwright Page
     * @param page Playwright Page instance
     * @param stepName Description of the action
     * @return Path to saved screenshot
     */
    public static String takeScreenshot(Page page, String stepName, String testcaseId) {
        if (page == null) {
            logger.error("Playwright Page is null. Cannot capture screenshot.");
            return null;
        }
        try {
            String fileName = generateFileName(stepName);
            String filePath = SCREENSHOT_DIR + File.separator + testcaseId + File.separator + fileName;
            // Create parent directory if needed
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            // Capture screenshot and save to file
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(filePath))
                    .setFullPage(false));
            // Read bytes and attach to Allure
            byte[] screenshotBytes = Files.readAllBytes(Paths.get(filePath));
            attachToAllure(screenshotBytes, stepName);
            logger.info("[SCREENSHOT] {} -> {}", stepName, filePath);
            return filePath;
        } catch (Exception e) {
            logger.error("Error capturing screenshot: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    /**
     * Take full-page screenshot for Playwright
     * @param page Playwright Page instance
     * @param stepName Description of the action
     * @return Path to saved screenshot
     */
    public static String takeFullPageScreenshot(Page page, String stepName) {
        if (page == null) {
            logger.error("Playwright Page is null. Cannot capture screenshot.");
            return null;
        }

        try {
            String fileName = generateFileName(stepName + "_fullpage");
            String filePath = SCREENSHOT_DIR + File.separator + fileName;

            // Capture full page screenshot
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(filePath))
                    .setFullPage(true));

            byte[] screenshotBytes = Files.readAllBytes(Paths.get(filePath));
            attachToAllure(screenshotBytes, stepName);
            logger.info("[SCREENSHOT] {} -> {}", stepName, filePath);
            return filePath;

        } catch (Exception e) {
            logger.error("Error capturing full-page screenshot: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    /**
     * Take screenshot of specific Playwright locator/element
     * @param locator Playwright Locator instance
     * @param stepName Description of the action
     * @return Path to saved screenshot
     */
    public static String takeElementScreenshot(
            com.microsoft.playwright.Locator locator,
            String stepName) {
        if (locator == null) {
            logger.error("Playwright Locator is null. Cannot capture screenshot.");
            return null;
        }

        try {
            String fileName = generateFileName(stepName + "_element");
            String filePath = SCREENSHOT_DIR + File.separator + fileName;

            // Capture element screenshot
            locator.screenshot(new com.microsoft.playwright.Locator.ScreenshotOptions()
                    .setPath(Paths.get(filePath)));

            byte[] screenshotBytes = Files.readAllBytes(Paths.get(filePath));
            attachToAllure(screenshotBytes, stepName);
            logger.info("[SCREENSHOT] {} -> {}", stepName, filePath);
            return filePath;

        } catch (Exception e) {
            logger.error("Error capturing element screenshot: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // ========== ALLURE ATTACHMENT ==========

    /**
     * Attach screenshot to Allure Report
     * @param screenshotBytes Image bytes
     * @param name Screenshot name/description
     */
    private static void attachToAllure(byte[] screenshotBytes, String name) {
        try {
            Allure.addAttachment(
                    name,
                    "image/png",
                    new ByteArrayInputStream(screenshotBytes),
                    "png"
            );
        } catch (Exception e) {
            logger.error("Error attaching screenshot to Allure: " + e.getMessage());
        }
    }

    /**
     * Get screenshot directory path
     */
    public static String getScreenshotDirectory() {
        return SCREENSHOT_DIR;
    }

    /**
     * Clear old screenshots (useful for cleanup)
     */
    public static void clearScreenshots() {
        try {
            //import org.apache.commons.io.FileUtils;
//            FileUtils.cleanDirectory(new File(SCREENSHOT_DIR));
            Files.list(Path.of(SCREENSHOT_DIR))
                    .forEach(path -> {
                        try {
                            Files.walk(path)
                                    .sorted(Comparator.reverseOrder())
                                    .forEach(p -> p.toFile().delete());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error clearing screenshots: " + e.getMessage());
        }
    }
}