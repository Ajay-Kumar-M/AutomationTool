package org.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PWCheck {
    public static void main(String[] args) {
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
    }
}
