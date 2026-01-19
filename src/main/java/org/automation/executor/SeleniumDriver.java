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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;


public class SeleniumDriver implements Driver {
    private WebDriver driver;
    private long implicitWait = 10;
    private long explicitWait = 5;
    private static final Logger logger = LoggerFactory.getLogger(SeleniumDriver.class);

//    public SeleniumDriver() {
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//    }

    @Override
    public void init(DriverConfigRecord driverConfigRecord) {
        String browserType = driverConfigRecord.browserType();
        if(driverConfigRecord.isDocker()){
            logger.info("Is docker container running:{}", DockerContainerCheck.isContainerRunning(driverConfigRecord.dockerContainerName()));
            if ((browserType.equalsIgnoreCase("chrome"))||(browserType.equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfigRecord.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (browserType.equalsIgnoreCase("firefox")) {
                FirefoxOptions options = new FirefoxOptions();
                try {
                    driver = new RemoteWebDriver(URI.create(driverConfigRecord.dockerUrl()).toURL(), options);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        } else {
            if ((browserType.equalsIgnoreCase("chrome"))||(browserType.equalsIgnoreCase("chromium"))) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--start-maximized");
                driver = new ChromeDriver(options);
            } else if (browserType.equalsIgnoreCase("firefox")) {
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
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Type in element")
    public void type(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        String text = getArg(actionRecord, 0);
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Click element")
    public void click(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        element.click();
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Clear element")
    public void clear(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        element.clear();
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Wait for Seconds")
    public void wait(ActionRecord actionRecord) {
        try {
            Thread.sleep(Long.parseLong(getArg(actionRecord, 0)) * 1000);
            ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Step("Scroll Page")
    public void scroll(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).scrollToElement(element).perform();
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
        /*
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        new Actions(driver).scrollToElement(element).perform();
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,500);");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[1].offsetTop;",container,element);
         */
    }

    @Step("Hover Element")
    public void hover(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(element).perform();
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Switch to Frame")
    public void frame(ActionRecord actionRecord) {
        By locator = parseLocator(actionRecord.locator());
        new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
        ScreenshotManager.takeScreenshot(driver, actionRecord.actionType(), actionRecord.testcaseId());
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
                case "isVisible":
                    WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + explicitWait + " seconds");
                    break;
                case "isVisibleTimeout":
                    WebElement element2 = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(getArg(actionRecord, 0))))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                    assertTrue(element2.isDisplayed(),"Element with locator '" + locator + "' was not visible within " + getArg(actionRecord, 0) + " seconds");
                    break;
                case "isHidden":
                    WebElement element3 = driver.findElement(locator);
                    assertFalse(element3.isDisplayed(),"Element with locator '" + locator + "' was not hidden");
                    break;
                case "isAttached":
                    List<WebElement> elements4 = driver.findElements(locator);
                    assertFalse(elements4.isEmpty(), "Element with locator '" + locator + "' was not attached");
                    break;
                case "isDetached":
                    List<WebElement> elements5 = driver.findElements(locator);
                    assertTrue(elements5.isEmpty(), "Element with locator '" + locator + "' was not detached");
                    break;
                case "isInViewport":
                    WebElement element6 = driver.findElement(locator);
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
            logger.error("Assert Visibility\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- TEXT CONTENT ---
    /** Assert element has exact text */
    @Step("Assert Text")
    public void assertText(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            switch (actionRecord.actionType()) {
                case "hasText":
                    assertEquals(element.getText(),getArg(actionRecord, 0),"Element with locator '" + locator + "' did not have text '"+ getArg(actionRecord, 0) +"'");
                    break;
                case "hasTextPattern":
                    assertTrue(element.getText().matches(getArg(actionRecord, 0)),"Element with locator '" + locator + "' did not have text pattern '"+ getArg(actionRecord, 0) +"'");
                    break;
                case "containsText":
                    assertTrue(element.getText().contains(getArg(actionRecord, 0)),"Element with locator '" + locator + "' did not contain '"+ getArg(actionRecord, 0) +"'");
                    break;
                case "containsTextPattern":
                    String regex = ".*" + getArg(actionRecord, 0) + ".*";
                    assertTrue(element.getText().matches(regex),"Element with locator '" + locator + "' did not contain text pattern '"+ getArg(actionRecord, 0) +"'");
                    break;
                case "hasTextMultipleElements":
                    List<WebElement> elements = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                            .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
                    List<String> expectedTexts = List.of(actionRecord.arguments());
                    assertEquals(
                            elements.size(),
                            expectedTexts.size(),
                            "Number of elements does not match expected text count"
                    );
                    for (int i = 0; i < elements.size(); i++) {
                        String actualText = elements.get(i).getText().trim();
                        String expectedText = expectedTexts.get(i);
                        assertTrue(actualText.equalsIgnoreCase(expectedText),"Element with locator '" + locator + "' did not have text '"+ expectedText +"'");
                    }
                    break;
                case "notContainsText":
                    assertFalse(element.getText().contains(getArg(actionRecord, 0)),"Element with locator '" + locator + "' contains '"+ getArg(actionRecord, 0) +"'");
                    break;
                default:
                    assertTrue(element.getText().equalsIgnoreCase(getArg(actionRecord, 0)),"Element with locator '" + locator + "' did not have text '"+ getArg(actionRecord, 0) +"'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Text\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- INPUT & FORM ---
    /** Assert input has specific value */
    @Step("Assert Element")
    public void assertElement(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            switch (actionRecord.actionType()) {
                case "hasValue":
                    assertEquals(element.getAttribute("value"), getArg(actionRecord, 0), "Element with locator '" + locator + "' did not have Value '" + getArg(actionRecord, 0) + "'");
                    break;
                case "isEditable":
                    assertTrue(element.isEnabled(),"Element with locator '" + locator + "' is not Enabled");
                    assertTrue(element.isDisplayed(),"Element with locator '" + locator + "' is not Displayed");
                    break;
                case "isChecked":
                    assertTrue(element.isSelected(),"Element with locator '" + locator + "' is not Checked");
                    break;
                case "isNotChecked":
                    assertFalse(element.isSelected(),"Element with locator '" + locator + "' is Checked");
                    break;
                case "hasValues":
                    List<WebElement> elements = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                            .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
                    List<String> expectedTexts = List.of(actionRecord.arguments());
                    assertEquals(
                            elements.size(),
                            expectedTexts.size(),
                            "Number of elements does not match expected text count"
                    );
                    for (int i = 0; i < elements.size(); i++) {
                        String actualText = elements.get(i).getAttribute("value");
                        String expectedText = expectedTexts.get(i);
                        assertTrue(actualText.equalsIgnoreCase(expectedText),"Element with locator '" + locator + "' did not have Value '"+ expectedText +"'");
                    }
                    break;
                case "isEnabled":
                    assertTrue(element.isEnabled(),"Element with locator '" + locator + "' is not Enabled");
                    break;
                case "isDisabled":
                    assertFalse(element.isEnabled(),"Element with locator '" + locator + "' is Enabled");
                    break;
                case "isFocused":
                    WebElement activeElement = driver.switchTo().activeElement();
                    assertEquals(
                            element,
                            activeElement,
                            "Expected element with locator '" + locator + "' to be focused"
                    );
                    break;
                case "hasId":
                    assertEquals(element.getAttribute("id"), getArg(actionRecord, 0), "Element with locator '" + locator + "' did not have ID '" + getArg(actionRecord, 0) + "'");
                    break;
                default:
                    assertEquals(element.getAttribute("value"), getArg(actionRecord, 0), "Element with locator '" + locator + "' did not have Value '" + getArg(actionRecord, 0) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Element\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- COUNT & LIST ---
    /** Assert locator resolves to exact count of elements */
    @Step("Asset Count|List")
    public void assertCount(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            List<WebElement> elements = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            switch (actionRecord.actionType()) {
                case "hasCount":
                    assertEquals(elements.size(),Integer.parseInt(getArg(actionRecord, 0)),"Element with locator '" + locator + "' does not have Count '" + getArg(actionRecord, 0) + "'");
                    break;
                case "exists":
                    assertNotEquals(0, elements.size(),"Element with locator '" + locator + "' does not Exist");
                    break;
                case "notExists", "isEmpty":
                    assertEquals(0, elements.size(),"Element with locator '" + locator + "' does Exist");
                    break;
                default:
                    assertEquals(elements.size(),Integer.parseInt(getArg(actionRecord, 0)),"Element with locator '" + locator + "' does not have Count '" + getArg(actionRecord, 0) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Count\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- PAGE ASSERTIONS ---
    /** Assert page has specific title (exact match) */
    @Step("Assert Page")
    public void assertPage(ActionRecord actionRecord) {
        try {
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            switch (actionRecord.actionType()) {
                case "hasTitle":
                    assertEquals(driver.getTitle(), getArg(actionRecord, 0),"Page does not have title '" + getArg(actionRecord, 0) + "'");
                    break;
                case "hasTitlePattern":
                    assertTrue(driver.getTitle().matches(getArg(actionRecord, 0)),"Page does not have title that matches the Pattern '" + getArg(actionRecord, 0) + "'");
                    break;
                case "hasURL":
                    assertEquals(driver.getCurrentUrl(), getArg(actionRecord, 0),"Page does not have URL '" + getArg(actionRecord, 0) + "'");
                    break;
                case "hasURLPattern":
                    assertTrue(driver.getCurrentUrl().matches(getArg(actionRecord, 0)),"Page does not have URL that matches the Pattern '" + getArg(actionRecord, 0) + "'");
                    break;
                default:
                    assertEquals(driver.getTitle(), getArg(actionRecord, 0),"Page does not have title '" + getArg(actionRecord, 0) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Page\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- CLASS & ATTRIBUTES ---
    /** Assert element has specific CSS class */
    @Step("Assert Class")
    public void assertClass(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            switch (actionRecord.actionType()) {
                case "hasClass":
                    assertEquals(element.getAttribute("class"), getArg(actionRecord, 0), "Element with locator '" + locator + "' did not have CSS Class '" + getArg(actionRecord, 0) + "'");
                    break;
                case "containsClass":
                    assertTrue(element.getAttribute("class").contains(getArg(actionRecord, 0)), "Element with locator '" + locator + "' did not contain CSS Class '" + getArg(actionRecord, 0) + "'");
                    break;
                case "notHasClass":
                    assertFalse(element.getAttribute("class").contains(getArg(actionRecord, 0)), "Element with locator '" + locator + "' did contain CSS Class '" + getArg(actionRecord, 0) + "'");
                    break;
                default:
                    assertEquals(element.getAttribute("class"), getArg(actionRecord, 0), "Element with locator '" + locator + "' did not have CSS Class '" + getArg(actionRecord, 0) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Class\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    /** Assert element has attribute (existence check) */
    @Step("Assert Attribute")
    public void assertAttribute(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            String attributeValue = element.getAttribute(getArg(actionRecord, 0));
            switch (actionRecord.actionType()) {
                case "hasAttribute":
                    assertNotNull(attributeValue, "Element with locator '" + locator + "' did not contain Attribute '" + getArg(actionRecord, 0) + "'");
                    break;
                case "hasAttributeValue":
                    assertEquals(attributeValue, getArg(actionRecord, 1), "Element with locator '" + locator + "' did not contain Attribute '" + getArg(actionRecord, 0) + "' with value '" +getArg(actionRecord, 1) + "'");
                    break;
                case "hasAttributePattern":
                    assertTrue(attributeValue.matches(getArg(actionRecord, 1)), "Element with locator '" + locator + "' did not match Attribute Pattern'" + getArg(actionRecord, 1) + "'");
                    break;
                case "notHasAttribute":
                    assertNull(attributeValue, "Element with locator '" + locator + "' contains Attribute '" + getArg(actionRecord, 0) + "'");
                    break;
                default:
                    assertNotNull(attributeValue, "Element with locator '" + locator + "' did not contain Attribute '" + getArg(actionRecord, 0) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Attribute\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    // --- CSS & STYLING ---
    /** Assert element has specific CSS property value */
    @Step("Assert CSS")
    public void assertCSSProperty(ActionRecord actionRecord) {
        try {
            By locator = parseLocator(actionRecord.locator());
            ScreenshotManager.takeScreenshot(driver, actionRecord.methodName() + " - " + actionRecord.actionType(), actionRecord.testcaseId());
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(explicitWait))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            String propertyValue = element.getCssValue(getArg(actionRecord, 0));
            switch (actionRecord.actionType()) {
                case "hasCSSProperty":
                    assertEquals(propertyValue, getArg(actionRecord, 1), "Element with locator '" + locator + "' did not contain Attribute '" + getArg(actionRecord, 0) + "' with value '" +getArg(actionRecord, 1) + "'");
                    break;
                case "hasCSSPropertyPattern":
                    assertTrue(propertyValue.matches(getArg(actionRecord, 1)), "Element with locator '" + locator + "' did not match Attribute Pattern '" + getArg(actionRecord, 0) + "' with value '" +getArg(actionRecord, 1) + "'");
                    break;
                default:
                    assertEquals(propertyValue, getArg(actionRecord, 1), "Element with locator '" + locator + "' did not contain Attribute '" + getArg(actionRecord, 0) + "' with value '" +getArg(actionRecord, 1) + "'");
                    break;
            }
        } catch (NoSuchElementException | TimeoutException | NullPointerException | AssertionError e) {
            logger.error("Assert Css Property\nassertion failed {}. message {}", e, e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}