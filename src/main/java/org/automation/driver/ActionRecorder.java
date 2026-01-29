package org.automation.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionRecorder {
    private final List<Map<String, Object>> actions = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void recordSession(String url, String outputJsonPath) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            injectRecorder(page);
            page.navigate(url);

            System.out.println("🌐 Browser launched! Close anytime - monitoring...");

            // ✅ REAL SOLUTION: Poll page state every 2 seconds
            pollForCompletion(page, outputJsonPath, 300); // 10 minutes max

            context.close();
            browser.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void pollForCompletion(Page page, String outputJsonPath, int maxMinutes) {
        int maxChecks = maxMinutes * 30; // 2s intervals
        int checkCount = 0;

        while (checkCount < maxChecks) {
            try {
                // Check if page still responsive
                Boolean isPageOpen = (Boolean) page.evaluate("true");

                // Extract actions every 10 seconds (5 checks)
                if (checkCount % 5 == 0) {
                    backupActions(page);
                    System.out.println("⏱️  Monitoring... (" + (actions.size()) + " actions)");
                }

                Thread.sleep(2000);
                checkCount++;

            } catch (PlaywrightException e) {
                // Page closed - extract immediately
                System.out.println("🔒 Page closed detected via exception");
                backupActions(page);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        saveActionsToJson(outputJsonPath);
    }

    private void injectRecorder(Page page) {
        page.addInitScript("""
            window.recordedActions = JSON.parse(localStorage.getItem('recordedActions') || '[]');
            let typingBuffer = {};
            
            function getSelector(el) {
                while (el?.nodeType !== 1) el = el.parentElement;
                if (!el) return 'body';
                if (el.id) return '#' + el.id;
                
                let path = [];
                let current = el;
                for (let i = 0; i < 4 && current; i++) {
                    let sel = current.tagName.toLowerCase();
                    if (current.className?.trim()) sel += '.' + current.className.trim().split(' ')[0];
                    path.unshift(sel);
                    current = current.parentElement;
                }
                return path.join(' > ');
            }
            
            // Click - immediate save
            document.addEventListener('click', e => {
                const selector = getSelector(e.target);
                window.recordedActions.push({
                    action: 'click',
                    selector: selector,
                    timestamp: Date.now()
                });
                localStorage.setItem('recordedActions', JSON.stringify(window.recordedActions));
            }, true);
            
            // Type - debounce to single action
            document.addEventListener('input', e => {
                if (e.target.matches('input, textarea')) {
                    const selector = getSelector(e.target);
                    typingBuffer[selector] = e.target.value;
                    
                    if (window.typeTimeout) clearTimeout(window.typeTimeout);
                    window.typeTimeout = setTimeout(() => {
                        window.recordedActions.push({
                            action: 'type',
                            selector: selector,
                            value: typingBuffer[selector],
                            timestamp: Date.now()
                        });
                        localStorage.setItem('recordedActions', JSON.stringify(window.recordedActions));
                        delete typingBuffer[selector];
                    }, 800);
                }
            }, true);
            
            console.log('🎬 Recorder active - close browser anytime');
            """);
    }

    private void backupActions(Page page) {
        try {
            Object actionsObj = page.evaluate("""
                () => {
                    try {
                        return JSON.parse(localStorage.getItem('recordedActions') || '[]');
                    } catch(e) {
                        return window.recordedActions || [];
                    }
                }
                """);

            System.out.println("local size "+actions.size());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recorded = (List<Map<String, Object>>) actionsObj;
            System.out.println("received size "+recorded.size());
            if (recorded != null && !recorded.isEmpty()) {
                actions.clear(); // Avoid duplicates
                actions.addAll(recorded);
            }
        } catch (Exception e) {
            // Page closed or unresponsive - localStorage has backup
            System.out.println("backupActions exception - "+e.getMessage());
        }
    }

    private void saveActionsToJson(String path) {
        // FINAL EXTRACTION: Fresh browser reads localStorage
        try (Playwright temp = Playwright.create()) {
            Browser tempBrowser = temp.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext tempContext = tempBrowser.newContext();
            Page tempPage = tempContext.newPage();

            tempPage.navigate("about:blank");
            Object finalActions = tempPage.evaluate("""
                () => {
                    try {
                        const actions = JSON.parse(localStorage.getItem('recordedActions') || '[]');
                        localStorage.removeItem('recordedActions'); // Cleanup
                        return actions;
                    } catch(e) {
                        return [];
                    }
                }
                """);

            System.out.println("saveActionsToJson local size "+actions.size());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> finalList = (List<Map<String, Object>>) finalActions;
            System.out.println("saveActionsToJson finallist size "+finalList.size());
            if (finalList != null && !finalList.isEmpty()) {
                actions.clear();
                actions.addAll(finalList);
            }

            Files.createDirectories(Paths.get(path).getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), actions);
            System.out.println("✅ SAVED " + actions.size() + " actions → " + path);

        } catch (Exception e) {
            System.err.println("Final save failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ActionRecorder().recordSession(
                "https://www.selenium.dev/selenium/web/fedcm/signin.html",
                "output/recorded-actions.json"
        );
    }
}

