package org.automation.driver;

import com.microsoft.playwright.*;
import org.automation.records.Action;

public class PlaywrightDriver implements Driver {
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String browserType = "chromium";

//    public PlaywrightDriver() {
//        Playwright playwright = Playwright.create();
//        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//        page = browser.newPage();
//    }

    @Override
    public void init(String browserName) {
        Playwright playwright = Playwright.create();
        if (browserName.equalsIgnoreCase("chrome") || browserName.equalsIgnoreCase("chromium")) {
            browser = playwright.chromium().launch();
        } else if (browserName.equalsIgnoreCase("firefox")) {
            browser = playwright.firefox().launch();
        } else if (browserName.equalsIgnoreCase("webkit")) {
            browser = playwright.webkit().launch();
        }
        context = browser.newContext();
        page = context.newPage();
    }

    @Override
    public void execute(Action action) {
        switch (action.action_type()) {
            case "gotoUrl" -> gotoUrl(action);
            case "type" -> type(action);
            case "click" -> click(action);
            case "clear" -> clear(action);
            case "wait" -> wait(action);
            default -> throw new IllegalArgumentException("Unknown action: " + action.action_type());
        }
    }

    void gotoUrl(Action action){
        page.navigate(getArg(action, 0));
    }

    void type(Action action){
        String selector = parseLocator(action.locator());
        page.fill(selector, getArg(action, 0));
    }

    void click(Action action){
        String selector = parseLocator(action.locator());
        page.click(selector);
    }

    void clear(Action action){
        page.fill(action.locator(), "");
    }

    void wait(Action action){
        try { Thread.sleep(Long.parseLong(getArg(action, 0)) * 1000); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public String getText(Action action) {
        String selector = parseLocator(action.locator());
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

    public void close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
    }

    private String parseLocator(String locator) {
        if (locator.startsWith("xpath:")) {
            return locator.replace("xpath:", "");
        } else if (locator.startsWith("css:")) {
            return locator.replace("css:", "");
        } else if (locator.startsWith("id:")) {
            return "#" + locator.replace("id:", "");
        }
        return locator;
    }
}