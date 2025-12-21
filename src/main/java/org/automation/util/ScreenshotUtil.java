package org.automation.util;

import com.microsoft.playwright.Page;
import org.automation.driver.PlaywrightDriver;
import org.automation.driver.SeleniumDriver;
import org.automation.driver.TestContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {
    public static String takeScreenshot(TestContext context, String testcaseId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "screenshot_" + testcaseId + "_" + timestamp + ".png";
            String path = "screenshots/" + filename;

            Files.createDirectories(Paths.get("screenshots"));
            if (context.driver() instanceof SeleniumDriver selenium) {
                // Selenium
                TakesScreenshot ts = (TakesScreenshot) selenium.getDriver();
                File src = ts.getScreenshotAs(OutputType.FILE);
                Files.copy(src.toPath(), Paths.get(path));
            } else if (context.driver() instanceof PlaywrightDriver pw) {
                // Playwright
                pw.getPage().screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)));
            }
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Failed to take screenshot", e);
        }
    }
}
