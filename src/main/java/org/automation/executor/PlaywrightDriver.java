package org.automation.executor;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import org.automation.driver.Driver;
import org.automation.records.ActionRecord;
import org.automation.records.DriverConfigRecord;
import org.automation.util.DockerContainerCheck;
import org.automation.util.ScreenshotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlaywrightDriver implements Driver {
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String browserType = "chromium";
    private Playwright playwright;
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightDriver.class);

//    public PlaywrightDriver() {
//        Playwright playwright = Playwright.create();
//        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//        page = browser.newPage();
//    }

    @Override
    public void init(DriverConfigRecord driverConfigRecord) {
        playwright = Playwright.create();
        String browserType = driverConfigRecord.browserType();
        if(driverConfigRecord.isDocker()){
            logger.info("Is docker container running: {}", DockerContainerCheck.isContainerRunning(driverConfigRecord.dockerContainerName()));
            if (browserType.equalsIgnoreCase("chrome") || browserType.equalsIgnoreCase("chromium")) {
                logger.info("Using docker- {}", driverConfigRecord.dockerUrl());
                browser = playwright.chromium().connect(driverConfigRecord.dockerUrl());
            } else if (browserType.equalsIgnoreCase("firefox")) {
                browser = playwright.firefox().connect(driverConfigRecord.dockerUrl());
            } else if (browserType.equalsIgnoreCase("webkit")) {
                browser = playwright.webkit().connect(driverConfigRecord.dockerUrl());
            }
        } else {
            if (browserType.equalsIgnoreCase("chrome") || browserType.equalsIgnoreCase("chromium")) {
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100) // 100 MS delay after every action
                );
            } else if (browserType.equalsIgnoreCase("firefox")) {
                browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100) // 100 MS delay after every action
                );
            } else if (browserType.equalsIgnoreCase("webkit")) {
                browser = playwright.webkit().launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100) // 100 MS delay after every action
                );
            }
        }
        context = browser.newContext();
        page = context.newPage();
    }

    public Page getPage() { return page; }

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

    @Step("Navigate to URL")
    public void gotoUrl(ActionRecord actionRecord){
        page.navigate(getArg(actionRecord, 0));
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Type Text")
    public void type(ActionRecord actionRecord){
        Locator locator = parseLocator(actionRecord);
        locator.fill(getArg(actionRecord, 0));
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Click Element")
    public void click(ActionRecord actionRecord){
        Locator locator = parseLocator(actionRecord);
        locator.click();
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Clear Text")
    public void clear(ActionRecord actionRecord){
        Locator locator = parseLocator(actionRecord);
        locator.fill("");
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Scroll Page")
    public void scroll(ActionRecord actionRecord){
        Locator locator = parseLocator(actionRecord);
        locator.scrollIntoViewIfNeeded();
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
        /*
        page.mouse().wheel(0, 500);   // scroll down
        page.mouse().wheel(0, -500);  // scroll up
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        page.keyboard().press("PageDown");
         */
    }

    @Step("Hover Element")
    public void hover(ActionRecord actionRecord){
        Locator locator = parseLocator(actionRecord);
        locator.hover();
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Wait for seconds")
    public void wait(ActionRecord actionRecord){
        try {
            page.waitForTimeout(Double.parseDouble(getArg(actionRecord, 0)));
            Thread.sleep(Long.parseLong(getArg(actionRecord, 0)) * 1000);
            ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Step("Switch to Frame")
    public void frame(ActionRecord actionRecord) {
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
    }

    @Step("Get Text")
    @Override
    public String getText(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.actionType(), actionRecord.testcaseId());
        return locator.textContent();
    }

    public boolean isElementPresent(ActionRecord actionRecord) {
        try {
            Locator locator = parseLocator(actionRecord);
            return locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    private String getArg(ActionRecord actionRecord, int index) {
        if (actionRecord.arguments() == null || actionRecord.arguments().length <= index) {
            throw new IllegalArgumentException("Missing argument");
        }
        return actionRecord.arguments()[index];
    }

    private Locator parseLocator(ActionRecord actionRecord) {
        String tempLocator = actionRecord.locator();
        if (tempLocator.startsWith("xpath:")) {
            tempLocator = tempLocator.replace("xpath:", "");
        } else if (tempLocator.startsWith("css:")) {
            tempLocator = tempLocator.replace("css:", "");
        } else if (tempLocator.startsWith("id:")) {
            tempLocator = "#" + tempLocator.replace("id:", "");
        } else if (tempLocator.startsWith("name:")) {
            tempLocator = "[name='"+tempLocator.replace("name:", "")+"']";
        } else if (tempLocator.startsWith("className:")) {
            tempLocator = "." + tempLocator.replace("className:", "");
        } else if (tempLocator.startsWith("tagName:")) {
            tempLocator = tempLocator.replace("tagName:", "");
        } else if (tempLocator.startsWith("linkText:")) {
            tempLocator = "text='" + tempLocator.replace("linkText:", "") + "'";
        }
        if ((actionRecord.frameLocator() != null) && ( !(actionRecord.frameLocator().isEmpty())) ) {
            return page.frameLocator(actionRecord.frameLocator()).locator(tempLocator);
        }
        return page.locator(tempLocator);
    }

    // ============================================================
    // ASSERTION METHODS
    // ============================================================

    // --- VISIBILITY & DOM STATE ---
    /** Assert element is visible on page */
    @Step("Assert Visibility")
    public void assertVisibility(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "isVisible": assertThat(locator).isVisible();
            break;
            case "isVisibleTimeout": assertThat(locator).isVisible(
                    new LocatorAssertions.IsVisibleOptions().setTimeout(Double.parseDouble(getArg(actionRecord,0))));
            break;
            case "isHidden": assertThat(locator).isHidden();
            break;
            case "isAttached": assertThat(locator).isAttached();
            break;
            case "isDetached": assertThat(locator).not().isAttached();
            break;
            case "isInViewport": assertThat(locator).isInViewport();
            break;
            default: assertThat(locator).isVisible();
            break;
        }
    }

    // --- TEXT CONTENT ---
    /** Assert element has exact text */
    @Step("Assert Text")
    public void assertText(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasText": assertThat(locator).hasText(getArg(actionRecord, 0));
                break;
            case "hasTextPattern": Pattern pattern1 = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
                assertThat(locator).hasText(pattern1);
                break;
            case "containsText": assertThat(locator).containsText(getArg(actionRecord, 0));
                break;
            case "containsTextPattern": Pattern pattern2 = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
                assertThat(locator).containsText(pattern2);
                break;
            case "hasTextMultipleElements": assertThat(locator).hasText(actionRecord.arguments());
                break;
            case "notContainsText": assertThat(locator).not().containsText(getArg(actionRecord, 0));
                break;
            default: assertThat(locator).hasText(getArg(actionRecord, 0));
                break;
        }
    }

    // --- INPUT & FORM ---
    /** Assert input has specific value */
    @Step("Assert Element")
    public void assertElement(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasValue": assertThat(locator).hasValue(getArg(actionRecord, 0));
                break;
            case "isEditable": assertThat(locator).isEditable();
                break;
            case "isChecked": assertThat(locator).isChecked();
                break;
            case "isNotChecked": assertThat(locator).not().isChecked();
                break;
            case "hasValues": assertThat(locator).hasValues(actionRecord.arguments());
                break;
            case "isEnabled": assertThat(locator).isEnabled();
                break;
            case "isDisabled": assertThat(locator).isDisabled();
                break;
            case "isFocused": assertThat(locator).isFocused();
                break;
            case "hasId": assertThat(locator).hasId(getArg(actionRecord, 0));
                break;
            default: assertThat(locator).hasValue(getArg(actionRecord, 0));
                break;
        }
    }

    // --- COUNT & LIST ---
    /** Assert locator resolves to exact count of elements */
    @Step("Asset Count|List")
    public void assertCount(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasCount": assertThat(locator).hasCount(Integer.parseInt(getArg(actionRecord, 0)));
                break;
            case "exists": assertThat(locator).not().hasCount(0);
                break;
            case "notExists": assertThat(locator).hasCount(0);
                break;
            case "isEmpty": assertThat(locator).isEmpty();
                break;
            default: assertThat(locator).hasCount(Integer.parseInt(getArg(actionRecord, 0)));
                break;
        }
    }

    // --- PAGE ASSERTIONS ---
    /** Assert page has specific title (exact match) */
    @Step("Assert Page")
    public void assertPage(ActionRecord actionRecord) {
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasTitle": assertThat(page).hasTitle(getArg(actionRecord, 0));
                break;
            case "hasTitlePattern": Pattern pattern1 = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page).hasTitle(pattern1);
                break;
            case "hasURL": assertThat(page).hasURL(getArg(actionRecord, 0));
                break;
            case "hasURLPattern": Pattern pattern2 = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page).hasURL(pattern2);
                break;
            default: assertThat(page).hasTitle(getArg(actionRecord, 0));
                break;
        }
    }

    // --- CLASS & ATTRIBUTES ---
    /** Assert element has specific CSS class */
    @Step("Assert Class")
    public void assertClass(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasClass":
                assertThat(locator).hasClass(getArg(actionRecord, 0));
                break;
            case "containsClass":
                assertThat(locator).containsClass(getArg(actionRecord, 0));
                break;
            case "notHasClass":
                assertThat(locator).not().hasClass(getArg(actionRecord, 0));
                break;
            default: assertThat(locator).hasClass(getArg(actionRecord, 0));
                break;
        }
    }

    /** Assert element has attribute (existence check) */
    @Step("Assert Attribute")
    public void assertAttribute(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasAttribute":
                assertThat(locator).hasAttribute(getArg(actionRecord, 0), Pattern.compile(".*"));
                break;
            case "hasAttributeValue":
                assertThat(locator).hasAttribute(getArg(actionRecord, 0), getArg(actionRecord, 1));
                break;
            case "hasAttributePattern":
                String attrName = getArg(actionRecord, 0);
                Pattern pattern = Pattern.compile(getArg(actionRecord, 1), Pattern.CASE_INSENSITIVE);
                assertThat(locator).hasAttribute(attrName, pattern);
                break;
            case "notHasAttribute":
                String attrName2 = getArg(actionRecord, 0);
                String value2 = getArg(actionRecord, 1);
                assertThat(locator).not().hasAttribute(attrName2, value2);
                break;
            default: assertThat(locator).hasAttribute(getArg(actionRecord, 0), Pattern.compile(".*"));
                break;
        }
    }

    // --- CSS & STYLING ---
    /** Assert element has specific CSS property value */
    @Step("Assert CSS")
    public void assertCSSProperty(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        String cssProperty = getArg(actionRecord, 0);
        ScreenshotManager.takeScreenshot(page, actionRecord.methodName()+" - "+ actionRecord.actionType(), actionRecord.testcaseId());
        switch (actionRecord.actionType()){
            case "hasCSSProperty":
                String value1 = getArg(actionRecord, 1);
                assertThat(locator).hasCSS(cssProperty, value1);
                break;
            case "hasCSSPropertyPattern":
                Pattern pattern = Pattern.compile(getArg(actionRecord, 1), Pattern.CASE_INSENSITIVE);
                assertThat(locator).hasCSS(cssProperty, pattern);
                break;
            default: String value2 = getArg(actionRecord, 1);
                assertThat(locator).hasCSS(cssProperty, value2);
                break;
        }
    }

    // --- ACCESSIBILITY ---
    /** Assert element has specific ARIA role */
    public void assertHasRole(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        AriaRole role = AriaRole.valueOf(getArg(actionRecord, 0).toUpperCase());
        assertThat(locator).hasRole(role);
    }

    /** Assert element has accessible name */
    public void assertHasAccessibleName(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        assertThat(locator).hasAccessibleName(getArg(actionRecord, 0));
    }

    /** Assert element has accessible name matching pattern */
    public void assertHasAccessibleNamePattern(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        Pattern pattern = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
        assertThat(locator).hasAccessibleName(pattern);
    }

    /** Assert element has accessible description */
    public void assertHasAccessibleDescription(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        assertThat(locator).hasAccessibleDescription(getArg(actionRecord, 0));
    }

    /** Assert element has accessible description matching pattern */
    public void assertHasAccessibleDescriptionPattern(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        Pattern pattern = Pattern.compile(getArg(actionRecord, 0), Pattern.CASE_INSENSITIVE);
        assertThat(locator).hasAccessibleDescription(pattern);
    }

    // --- JAVASCRIPT PROPERTIES ---
    /** Assert element has specific JavaScript property value */
    public void assertHasJSProperty(ActionRecord actionRecord) {
        Locator locator = parseLocator(actionRecord);
        String propertyName = getArg(actionRecord, 0);
        String value =getArg(actionRecord, 1);
        assertThat(locator).hasJSProperty(propertyName, value);
    }

    /** Assert with custom timeout and retry logic */
    public void assertWithRetry(AssertionCheck check, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        AssertionError lastError = null;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                check.validate();
                return; // Success
            } catch (AssertionError e) {
                lastError = e;
                try {
                    Thread.sleep(100); // 100ms retry interval
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (lastError != null) {
            throw lastError;
        }
    }

    /** Assert with custom failure message for better reporting */
    public void assertWithMessage(Runnable assertion, String message) {
        try {
            assertion.run();
        } catch (AssertionError e) {
            throw new AssertionError(message + " | Original: " + e.getMessage(), e);
        }
    }

    /** Functional interface for custom assertion validation */
    @FunctionalInterface
    public interface AssertionCheck {
        void validate() throws AssertionError;
    }

    public void close() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

}

/*
String wsEndpoint = "ws://0.0.0.0:3000/"; // or "ws://127.0.0.1:3000/" depending on config

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().connect(wsEndpoint);
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://example.com");
            System.out.println("Title: " + page.title());

            context.close();
            browser.close();
        }
 */

/*
/** Assert element is visible with custom timeout (ms)
public void assertVisibleTimeout(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(Double.parseDouble(getArg(action,1))));
}

/** Assert element is hidden
public void assertHidden(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isHidden();
}

/** Assert element is attached to DOM
public void assertAttached(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isAttached();
}

/** Assert element is detached from DOM
public void assertDetached(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).not().isAttached();
}

/** Assert element is in viewport
public void assertInViewport(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isInViewport();
}
 */

/*

    /** Assert element has text matching regex pattern
public void assertHasTextPattern(Action action) {
    String selector = parseLocator(action.locator());
    Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
    assertThat(page.locator(selector)).hasText(pattern);
}

/** Assert element contains substring (partial match)
public void assertContainsText(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).containsText(getArg(action, 0));
}

/** Assert element contains text matching pattern
public void assertContainsTextPattern(Action action) {
    String selector = parseLocator(action.locator());
    Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
    assertThat(page.locator(selector)).containsText(pattern);
}

/** Assert multiple elements have text in exact order (lists/tables)
public void assertHasTextMultipleElements(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).hasText(action.arguments());
}

/** Assert element does NOT contain text
public void assertNotContainsText(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).not().containsText(getArg(action, 0));
}
 */

/*

    /** Assert element is editable
public void assertEditable(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isEditable();
}

/** Assert checkbox/radio is checked
public void assertChecked(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isChecked();
}

/** Assert checkbox/radio is unchecked
public void assertUnchecked(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).not().isChecked();
}

/** Assert select element has specific options selected
public void assertSelectHasValues(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).hasValues(action.arguments());
}

    // --- BUTTONS & INTERACTIVE ELEMENTS ---
    /** Assert element is enabled
public void assertEnabled(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isEnabled();
}

/** Assert element is disabled
public void assertDisabled(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isDisabled();
}

/** Assert element has focus
public void assertFocused(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isFocused();
}
    /** Assert element has specific ID
public void assertHasId(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).hasId(getArg(action, 0));
}
 */

/*
    /** Assert element exists (at least one match)
public void assertExists(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).not().hasCount(0);
}

/** Assert element does NOT exist (zero matches)
public void assertNotExists(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).hasCount(0);
}

/** Assert container is empty (no children)
public void assertEmpty(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).isEmpty();
}
 */

/*
    /** Assert page title matches pattern
public void assertPageTitlePattern(Action action) {
    Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
    assertThat(page).hasTitle(pattern);
}

/** Assert page URL has specific value
public void assertPageURL(Action action) {
    assertThat(page).hasURL(getArg(action, 0));
}

/** Assert page URL matches pattern
public void assertPageURLPattern(Action action) {
    Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
    assertThat(page).hasURL(pattern);
}
 */

/*
    /** Assert element contains CSS class
public void assertContainsClass(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).containsClass(getArg(action, 0));
}

/** Assert element does NOT have class
public void assertNotHasClass(Action action) {
    String selector = parseLocator(action.locator());
    assertThat(page.locator(selector)).not().hasClass(getArg(action, 0));
}
 */

/*

    /** Assert element has attribute with exact value
public void assertHasAttributeValue(Action action) {
    String selector = parseLocator(action.locator());
    String attrName = getArg(action, 0);
    String attrValue = getArg(action, 1);
    assertThat(page.locator(selector)).hasAttribute(attrName, attrValue);
}

/** Assert element has attribute matching pattern
public void assertHasAttributePattern(Action action) {
    String selector = parseLocator(action.locator());
    String attrName = getArg(action, 0);
    Pattern pattern = Pattern.compile(getArg(action, 1), Pattern.CASE_INSENSITIVE);
    assertThat(page.locator(selector)).hasAttribute(attrName, pattern);
}

/** Assert element does NOT have attribute with value
public void assertNotHasAttribute(Action action) {
    String selector = parseLocator(action.locator());
    String attrName = getArg(action, 0);
    String value = getArg(action, 1);
    assertThat(page.locator(selector)).not().hasAttribute(attrName, value);
}
 */

/*

    /** Assert element has CSS property matching pattern
public void assertHasCSSPattern(Action action) {
    String selector = parseLocator(action.locator());
    String cssProperty = getArg(action, 0);
    Pattern pattern = Pattern.compile(getArg(action, 1), Pattern.CASE_INSENSITIVE);
    assertThat(page.locator(selector)).hasCSS(cssProperty, pattern);
}
 */