package org.automation.executor;

import io.qameta.allure.Step;
import org.automation.driver.Driver;
import org.automation.records.ActionRecord;
import org.automation.records.DriverConfigRecord;
import org.automation.util.DockerContainerCheck;
import org.automation.util.ScreenshotManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.awt.Rectangle;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SeleniumDriver implements Driver {
    private WebDriver driver;
    private long implicitWait = 10;
    private long explicitWait = 5;

//    public SeleniumDriver() {
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//    }

    @Override
    public void init(DriverConfigRecord driverConfigRecord) {
        if(driverConfigRecord.isDocker()){
            System.out.println("Is docker container running:"+ DockerContainerCheck.isContainerRunning(driverConfigRecord.dockerContainerName()));
            if ((driverConfigRecord.browserType().equalsIgnoreCase("chrome"))||(driverConfigRecord.browserType().equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfigRecord.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (driverConfigRecord.browserType().equalsIgnoreCase("firefox")) {
                FirefoxOptions options = new FirefoxOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfigRecord.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        } else {
            if ((driverConfigRecord.browserType().equalsIgnoreCase("chrome"))||(driverConfigRecord.browserType().equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--start-maximized");
                driver = new ChromeDriver(options);
            } else if (driverConfigRecord.browserType().equalsIgnoreCase("firefox")) {
                driver = new FirefoxDriver();
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().window().maximize();
        }
    }

    public WebDriver getDriver() { return driver; }

    @Override
    public void execute(ActionRecord actionRecord) {
        switch (actionRecord.actionType()) {
            case "gotoUrl" -> gotoUrl(actionRecord);
            case "type" -> type(actionRecord);
            case "click" -> click(actionRecord);
            case "clear" -> clear(actionRecord);
            case "wait" -> wait(actionRecord);
            case "assertVisibility" -> assertVisibility(actionRecord);
            default -> throw new IllegalArgumentException("Unknown action: " + actionRecord.actionType());
        }
    }

    @Override
    public String getText(ActionRecord actionRecord) {
        return "";
    }

    @Step("Navigate to URL")
    public void gotoUrl(ActionRecord actionRecord) {
//        Allure.step("Navigate to URL: " + getArg(action, 0));
        String url = getArg(actionRecord, 0);
        driver.get(url);
        ScreenshotManager.takeScreenshot(driver, "gotoUrl", actionRecord.testcaseId());
    }

    @Step("Type in element")
    public void type(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        String text = getArg(actionRecord, 0);
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(text);
        ScreenshotManager.takeScreenshot(driver, "type", actionRecord.testcaseId());
    }

    @Step("Click element")
    public void click(ActionRecord actionRecord) {
        By by = parseLocator(actionRecord.locator());
        driver.findElement(by).click();
        ScreenshotManager.takeScreenshot(driver, "click", actionRecord.testcaseId());
    }

    @Step("Clear element")
    public void clear(ActionRecord actionRecord) {
        By by = parseLocator(actionRecord.locator());
        driver.findElement(by).clear();
        ScreenshotManager.takeScreenshot(driver, "clear", actionRecord.testcaseId());
    }

    @Step("Wait for Seconds")
    public void wait(ActionRecord actionRecord) {
        try {
            Thread.sleep(Long.parseLong(getArg(actionRecord, 0)) * 1000);
            ScreenshotManager.takeScreenshot(driver, "wait", actionRecord.testcaseId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String getArg(ActionRecord actionRecord, int index) {
        if (actionRecord.arguments() == null || actionRecord.arguments().length <= index) {
            throw new IllegalArgumentException("Missing argument " + index + " for " + actionRecord.actionType());
        }
        return actionRecord.arguments()[index];
    }

    @Step("Get element text")
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
        if ((locator.startsWith("xpath:"))||(locator.startsWith("/"))||(locator.startsWith("//"))) {
            return By.xpath(locator.replace("xpath:", ""));
        } else if ((locator.startsWith("css:"))||(locator.startsWith("#"))||(locator.startsWith("."))) {
            return By.cssSelector(locator.replace("css:", ""));
        } else if (locator.startsWith("id:")) {
            return By.id(locator.replace("id:", ""));
        } else if (locator.startsWith("name:")) {
            return By.name(locator.replace("name:", ""));
        } else if (locator.startsWith("className:")) {
            return By.className(locator.replace("className:", ""));
        } else if (locator.startsWith("tagName:")) {
            return By.tagName(locator.replace("tagName:", ""));
        } else if (locator.startsWith("linkText:")) {
            return By.linkText(locator.replace("linkText:", ""));
        }
        return By.cssSelector(locator);
    }

    @Step("Assert Visibility")
    public void assertVisibility(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
//            WebElement element = driver.findElement(locator);
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
            switch (actionRecord.actionType()){
                case "isVisible": WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + explicitWait + " seconds");
                    break;
                case "isVisibleTimeout": WebElement element2 = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(getArg(actionRecord, 0))))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element2.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + getArg(actionRecord, 0) + " seconds");
                    break;
                case "isHidden": WebElement element3 = driver.findElement(locator);
                    assertFalse(element3.isDisplayed(),"Element with locator '" + locator + "' was not hidden");
                    break;
                case "isAttached": List<WebElement> elements4 = driver.findElements(locator);
                    assertFalse(elements4.isEmpty(), "Element with locator '" + locator + "' was not attached");
                    break;
                case "isDetached": List<WebElement> elements5 = driver.findElements(locator);
                    assertTrue(elements5.isEmpty(), "Element with locator '" + locator + "' was not detached");
                    break;
                case "isInViewport": WebElement element6 = driver.findElement(locator);
                    java.awt.Rectangle elemRect = new java.awt.Rectangle(
                            element6.getRect().getX(),
                            element6.getRect().getY(),
                            element6.getRect().getWidth(),
                            element6.getRect().getHeight()
                    );
                    int viewWidth = ((Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientWidth")).intValue();
                    int viewHeight = ((Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientHeight")).intValue();
                    Rectangle viewRect = new Rectangle(0, 0, viewWidth, viewHeight);
                    assertTrue(viewRect.intersects(elemRect));
                    break;
                default: WebElement element7 = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element7.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + explicitWait + " seconds");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            System.out.println("\ncatch called " + e + ". message " + e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}