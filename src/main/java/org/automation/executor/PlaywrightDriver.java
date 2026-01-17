package org.automation.executor;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import org.automation.records.Action;
import org.automation.records.DriverConfig;
import org.automation.util.DockerContainerCheck;
import org.automation.util.ScreenshotManager;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlaywrightDriver implements Driver {
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String browserType = "chromium";
    private Playwright playwright;

//    public PlaywrightDriver() {
//        Playwright playwright = Playwright.create();
//        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//        page = browser.newPage();
//    }

    @Override
    public void init(DriverConfig driverConfig) {
        playwright = Playwright.create();
        if(driverConfig.isDocker()){
            System.out.println("Is docker container running:"+ DockerContainerCheck.isContainerRunning(driverConfig.dockerContainerName()));
            if (driverConfig.browserType().equalsIgnoreCase("chrome") || driverConfig.browserType().equalsIgnoreCase("chromium")) {
                System.out.println("Using docker-"+driverConfig.dockerUrl());
                browser = playwright.chromium().connect(driverConfig.dockerUrl());
            } else if (driverConfig.browserType().equalsIgnoreCase("firefox")) {
                browser = playwright.firefox().connect(driverConfig.dockerUrl());
            } else if (driverConfig.browserType().equalsIgnoreCase("webkit")) {
                browser = playwright.webkit().connect(driverConfig.dockerUrl());
            }
        } else {
            if (driverConfig.browserType().equalsIgnoreCase("chrome") || driverConfig.browserType().equalsIgnoreCase("chromium")) {
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100) // 100 MS delay after every action
                );
            } else if (driverConfig.browserType().equalsIgnoreCase("firefox")) {
                browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100) // 100 MS delay after every action
                );
            } else if (driverConfig.browserType().equalsIgnoreCase("webkit")) {
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

    @Step("Navigate to URL")
    public void gotoUrl(Action action){
        page.navigate(getArg(action, 0));
        ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
    }

    @Step("Type Text")
    public void type(Action action){
        String selector = parseLocator(action.locator());
        page.fill(selector, getArg(action, 0));
        ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
    }

    @Step("Click Element")
    public void click(Action action){
        String selector = parseLocator(action.locator());
        page.click(selector);
        ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
    }

    @Step("Clear Text")
    public void clear(Action action){
        page.fill(action.locator(), "");
        ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
    }

    @Step("Wait for seconds")
    public void wait(Action action){
        try {
            page.waitForTimeout(Double.parseDouble(getArg(action, 0)));
            Thread.sleep(Long.parseLong(getArg(action, 0)) * 1000);
            ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Step("Get Text")
    @Override
    public String getText(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.actionType(),action.testcaseId());
        return page.textContent(selector);
    }

    public boolean isElementPresent(Action action) {
        try {
            String selector = parseLocator(action.locator());
            return page.locator(selector).isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    private String getArg(Action action, int index) {
        if (action.arguments() == null || action.arguments().length <= index) {
            throw new IllegalArgumentException("Missing argument");
        }
        return action.arguments()[index];
    }

    private String parseLocator(String locator) {
        if (locator.startsWith("xpath:")) {
            return locator.replace("xpath:", "");
        } else if (locator.startsWith("css:")) {
            return locator.replace("css:", "");
        } else if (locator.startsWith("id:")) {
            return "#" + locator.replace("id:", "");
        } else if (locator.startsWith("name:")) {
            return "[name='"+locator.replace("name:", "")+"']";
        } else if (locator.startsWith("className:")) {
            return "." + locator.replace("className:", "");
        } else if (locator.startsWith("tagName:")) {
            return locator.replace("tagName:", "");
        } else if (locator.startsWith("linkText:")) {
            return "text='" + locator.replace("linkText:", "") + "'";
        }
        return locator;
    }

    // ============================================================
    // ASSERTION METHODS
    // ============================================================

    // --- VISIBILITY & DOM STATE ---
    /** Assert element is visible on page */
    @Step("Assert Visibility")
    public void assertVisibility(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "isVisible": assertThat(page.locator(selector)).isVisible();
            break;
            case "isVisibleTimeout": assertThat(page.locator(selector)).isVisible(
                    new LocatorAssertions.IsVisibleOptions().setTimeout(Double.parseDouble(getArg(action,0))));
            break;
            case "isHidden": assertThat(page.locator(selector)).isHidden();
            break;
            case "isAttached": assertThat(page.locator(selector)).isAttached();
            break;
            case "isDetached": assertThat(page.locator(selector)).not().isAttached();
            break;
            case "isInViewport": assertThat(page.locator(selector)).isInViewport();
            break;
            default: assertThat(page.locator(selector)).isVisible();
            break;
        }
    }

    // --- TEXT CONTENT ---
    /** Assert element has exact text */
    @Step("Assert Text")
    public void assertText(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasText": assertThat(page.locator(selector)).hasText(getArg(action, 0));
                break;
            case "hasTextPattern": Pattern pattern1 = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page.locator(selector)).hasText(pattern1);
                break;
            case "containsText": assertThat(page.locator(selector)).containsText(getArg(action, 0));
                break;
            case "containsTextPattern": Pattern pattern2 = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page.locator(selector)).containsText(pattern2);
                break;
            case "hasTextMultipleElements": assertThat(page.locator(selector)).hasText(action.arguments());
                break;
            case "notContainsText": assertThat(page.locator(selector)).not().containsText(getArg(action, 0));
                break;
            default: assertThat(page.locator(selector)).hasText(getArg(action, 0));
                break;
        }
    }

    // --- INPUT & FORM ---
    /** Assert input has specific value */
    @Step("Assert Element")
    public void assertElement(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasValue": assertThat(page.locator(selector)).hasValue(getArg(action, 0));
                break;
            case "isEditable": assertThat(page.locator(selector)).isEditable();
                break;
            case "isChecked": assertThat(page.locator(selector)).isChecked();
                break;
            case "isNotChecked": assertThat(page.locator(selector)).not().isChecked();
                break;
            case "hasValues": assertThat(page.locator(selector)).hasValues(action.arguments());
                break;
            case "isEnabled": assertThat(page.locator(selector)).isEnabled();
                break;
            case "isDisabled": assertThat(page.locator(selector)).isDisabled();
                break;
            case "isFocused": assertThat(page.locator(selector)).isFocused();
                break;
            case "hasId": assertThat(page.locator(selector)).hasId(getArg(action, 0));
                break;
            default: assertThat(page.locator(selector)).hasValue(getArg(action, 0));
                break;
        }
    }

    // --- COUNT & LIST ---
    /** Assert locator resolves to exact count of elements */
    @Step("Asset Count|List")
    public void assertCount(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasCount": assertThat(page.locator(selector)).hasCount(Integer.parseInt(getArg(action, 0)));
                break;
            case "exists": assertThat(page.locator(selector)).not().hasCount(0);
                break;
            case "notExists": assertThat(page.locator(selector)).hasCount(0);
                break;
            case "isEmpty": assertThat(page.locator(selector)).isEmpty();
                break;
            default: assertThat(page.locator(selector)).hasCount(Integer.parseInt(getArg(action, 0)));
                break;
        }
    }

    // --- PAGE ASSERTIONS ---
    /** Assert page has specific title (exact match) */
    @Step("Assert Page")
    public void assertPage(Action action) {
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasTitle": assertThat(page).hasTitle(getArg(action, 0));
                break;
            case "hasTitlePattern": Pattern pattern1 = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page).hasTitle(pattern1);
                break;
            case "hasURL": assertThat(page).hasURL(getArg(action, 0));
                break;
            case "hasURLPattern": Pattern pattern2 = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
                assertThat(page).hasURL(pattern2);
                break;
            default: assertThat(page).hasTitle(getArg(action, 0));
                break;
        }
    }

    // --- CLASS & ATTRIBUTES ---
    /** Assert element has specific CSS class */
    @Step("Assert Class")
    public void assertClass(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasClass": assertThat(page.locator(selector)).hasClass(getArg(action, 0));
                break;
            case "containsClass": assertThat(page.locator(selector)).containsClass(getArg(action, 0));
                break;
            case "notHasClass": assertThat(page.locator(selector)).not().hasClass(getArg(action, 0));
                break;
            default: assertThat(page.locator(selector)).hasClass(getArg(action, 0));
                break;
        }
    }

    /** Assert element has attribute (existence check) */
    @Step("Assert Attribute")
    public void assertAttribute(Action action) {
        String selector = parseLocator(action.locator());
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasAttribute": assertThat(page.locator(selector)).hasAttribute(getArg(action, 0), Pattern.compile(".*"));
                break;
            case "hasAttributeValue": assertThat(page.locator(selector)).hasAttribute(getArg(action, 0), getArg(action, 1));
                break;
            case "hasAttributePattern": String attrName = getArg(action, 0);
                Pattern pattern = Pattern.compile(getArg(action, 1), Pattern.CASE_INSENSITIVE);
                assertThat(page.locator(selector)).hasAttribute(attrName, pattern);
                break;
            case "notHasAttribute": String attrName2 = getArg(action, 0);
                String value2 = getArg(action, 1);
                assertThat(page.locator(selector)).not().hasAttribute(attrName2, value2);
                break;
            default: assertThat(page.locator(selector)).hasAttribute(getArg(action, 0), Pattern.compile(".*"));
                break;
        }
    }

    // --- CSS & STYLING ---
    /** Assert element has specific CSS property value */
    @Step("Assert CSS")
    public void assertCSS(Action action) {
        String selector = parseLocator(action.locator());
        String cssProperty = getArg(action, 0);
        ScreenshotManager.takeScreenshot(page,action.methodName()+" - "+action.actionType(),action.testcaseId());
        switch (action.actionType()){
            case "hasCSS": String value1 = getArg(action, 1);
                assertThat(page.locator(selector)).hasCSS(cssProperty, value1);
                break;
            case "hasCSSPattern": Pattern pattern = Pattern.compile(getArg(action, 1), Pattern.CASE_INSENSITIVE);
                assertThat(page.locator(selector)).hasCSS(cssProperty, pattern);
                break;
            default: String value2 = getArg(action, 1);
                assertThat(page.locator(selector)).hasCSS(cssProperty, value2);
                break;
        }
    }

    // --- ACCESSIBILITY ---
    /** Assert element has specific ARIA role */
    public void assertHasRole(Action action) {
        String selector = parseLocator(action.locator());
        AriaRole role = AriaRole.valueOf(getArg(action, 0).toUpperCase());
        assertThat(page.locator(selector)).hasRole(role);
    }

    /** Assert element has accessible name */
    public void assertHasAccessibleName(Action action) {
        String selector = parseLocator(action.locator());
        assertThat(page.locator(selector)).hasAccessibleName(getArg(action, 0));
    }

    /** Assert element has accessible name matching pattern */
    public void assertHasAccessibleNamePattern(Action action) {
        String selector = parseLocator(action.locator());
        Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
        assertThat(page.locator(selector)).hasAccessibleName(pattern);
    }

    /** Assert element has accessible description */
    public void assertHasAccessibleDescription(Action action) {
        String selector = parseLocator(action.locator());
        assertThat(page.locator(selector)).hasAccessibleDescription(getArg(action, 0));
    }

    /** Assert element has accessible description matching pattern */
    public void assertHasAccessibleDescriptionPattern(Action action) {
        String selector = parseLocator(action.locator());
        Pattern pattern = Pattern.compile(getArg(action, 0), Pattern.CASE_INSENSITIVE);
        assertThat(page.locator(selector)).hasAccessibleDescription(pattern);
    }

    // --- JAVASCRIPT PROPERTIES ---
    /** Assert element has specific JavaScript property value */
    public void assertHasJSProperty(Action action) {
        String selector = parseLocator(action.locator());
        String propertyName = getArg(action, 0);
        String value =getArg(action, 1);
        assertThat(page.locator(selector)).hasJSProperty(propertyName, value);
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