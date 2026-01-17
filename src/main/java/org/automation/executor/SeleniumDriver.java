package org.automation.executor;

import com.microsoft.playwright.assertions.LocatorAssertions;
import io.qameta.allure.Step;
import org.automation.records.Action;
import org.automation.records.DriverConfig;
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
    public void init(DriverConfig driverConfig) {
        if(driverConfig.isDocker()){
            System.out.println("Is docker container running:"+ DockerContainerCheck.isContainerRunning(driverConfig.dockerContainerName()));
            if ((driverConfig.browserType().equalsIgnoreCase("chrome"))||(driverConfig.browserType().equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfig.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (driverConfig.browserType().equalsIgnoreCase("firefox")) {
                FirefoxOptions options = new FirefoxOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfig.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        } else {
            if ((driverConfig.browserType().equalsIgnoreCase("chrome"))||(driverConfig.browserType().equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--start-maximized");
                driver = new ChromeDriver(options);
            } else if (driverConfig.browserType().equalsIgnoreCase("firefox")) {
                driver = new FirefoxDriver();
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().window().maximize();
        }
    }

    public WebDriver getDriver() { return driver; }

    @Override
    public void execute(Action action) {
        switch (action.actionType()) {
            case "gotoUrl" -> gotoUrl(action);
            case "type" -> type(action);
            case "click" -> click(action);
            case "clear" -> clear(action);
            case "wait" -> wait(action);
            case "assertVisibility" -> assertVisibility(action);
            default -> throw new IllegalArgumentException("Unknown action: " + action.actionType());
        }
    }

    @Override
    public String getText(Action action) {
        return "";
    }

    @Step("Navigate to URL")
    public void gotoUrl(Action action) {
//        Allure.step("Navigate to URL: " + getArg(action, 0));
        String url = getArg(action, 0);
        driver.get(url);
        ScreenshotManager.takeScreenshot(driver, "gotoUrl", action.testcaseId());
    }

    @Step("Type in element")
    public void type(Action action) {
        By locator = parseLocator(action.locator());
        String text = getArg(action, 0);
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(text);
        ScreenshotManager.takeScreenshot(driver, "type", action.testcaseId());
    }

    @Step("Click element")
    public void click(Action action) {
        By by = parseLocator(action.locator());
        driver.findElement(by).click();
        ScreenshotManager.takeScreenshot(driver, "click", action.testcaseId());
    }

    @Step("Clear element")
    public void clear(Action action) {
        By by = parseLocator(action.locator());
        driver.findElement(by).clear();
        ScreenshotManager.takeScreenshot(driver, "clear", action.testcaseId());
    }

    @Step("Wait for Seconds")
    public void wait(Action action) {
        try {
            Thread.sleep(Long.parseLong(getArg(action, 0)) * 1000);
            ScreenshotManager.takeScreenshot(driver, "wait", action.testcaseId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String getArg(Action action, int index) {
        if (action.arguments() == null || action.arguments().length <= index) {
            throw new IllegalArgumentException("Missing argument " + index + " for " + action.actionType());
        }
        return action.arguments()[index];
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
    public void assertVisibility(Action action) {
        try {
            By locator = parseLocator(action.locator());
//            WebElement element = driver.findElement(locator);
            ScreenshotManager.takeScreenshot(driver,action.methodName()+" - "+action.actionType(),action.testcaseId());
            switch (action.actionType()){
                case "isVisible": WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + explicitWait + " seconds");
                    break;
                case "isVisibleTimeout": WebElement element2 = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(getArg(action, 0))))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element2.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + getArg(action, 0) + " seconds");
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