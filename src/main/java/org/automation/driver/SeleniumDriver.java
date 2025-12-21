package org.automation.driver;

import org.automation.records.Action;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.time.Duration;

public class SeleniumDriver implements Driver {
    private WebDriver driver;
    private long implicitWait = 10;

//    public SeleniumDriver() {
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//    }

    @Override
    public void init(String browser) {
        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().window().maximize();
    }

    @Override
        public void execute(Action action) {
            switch (action.action_type()) {
                case "gotoUrl" -> gotoUrl(action);
                case "type" -> type(action);
                case "click" -> click(action);
                case "clear" -> clear(action);
                case "wait" -> waitForSeconds(action);
                // Add more as needed
                default -> throw new IllegalArgumentException("Unknown action: " + action.action_type());
            }
        }

        private void gotoUrl(Action action) {
            String url = getArg(action, 0);
            driver.get(url);
        }

        private void type(Action action) {
            By locator = parseLocator(action.locator());
            String text = getArg(action, 0);
            driver.findElement(locator).clear();
            driver.findElement(locator).sendKeys(text);
        }

        private void click(Action action) {
            By by = parseLocator(action.locator());
            driver.findElement(by).click();
        }

        private void clear(Action action) {
            By by = parseLocator(action.locator());
            driver.findElement(by).clear();
        }

        private void waitForSeconds(Action action) {
            try {
                Thread.sleep(Long.parseLong(getArg(action, 0)) * 1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String getArg(Action action, int index) {
            if (action.arguments() == null || action.arguments().length <= index) {
                throw new IllegalArgumentException("Missing argument " + index + " for " + action.action_type());
            }
            return action.arguments()[index];
        }

        public String getText(String locator) {
            By by = parseLocator(locator);
            return driver.findElement(by).getText();
        }

        public boolean isElementPresent(String locator) {
            try {
                By by = parseLocator(locator);
                driver.findElement(by);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private By parseLocator(String locator) {
            if (locator.startsWith("xpath:")) {
                return By.xpath(locator.replace("xpath:", ""));
            } else if (locator.startsWith("css:")) {
                return By.cssSelector(locator.replace("css:", ""));
            } else if (locator.startsWith("id:")) {
                return By.id(locator.replace("id:", ""));
            }
            return By.xpath(locator);
        }

        public void close() {
            if (driver != null) driver.quit();
        }
    }