package org.automation.driver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import org.automation.records.ActionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionRecorder {

    private final List<ActionRecord> actions = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ActionRecorder.class);

    public void recordSession(String startUrl, String outputJsonPath, String testcaseId, String epic, String feature, String story, String description) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50)
            );
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setViewportSize(1280, 800)
            );
            Page page = context.newPage();

            page.onConsoleMessage(msg -> {
                String text = msg.text();
                if (text.startsWith("ACTION:")) {
//                    try {
//                        String json = text.substring(7);
//                        @SuppressWarnings("unchecked")
//                        Map<String, Object> action = mapper.readValue(json, Map.class);
//                        if (!actions.contains(action)) {  // simple dedup by content
//                            actions.add(action);
//                            logger.info("→ Instant capture via console: " + action);
//                        }
//                    } catch (Exception e) {
//                        System.err.println("Invalid ACTION JSON: " + text + " → " + e);
//                    }
                    logger.info("Browser Action - {}", text);
                } else {
                    // Optional: uncomment to see all console logs
                    logger.info("Browser Console: {}", text);
                }
            });
            injectRecorderScript(page);
            page.navigate(startUrl);
            actions.add(new ActionRecord(
                    "gotoUrl","", new String[]{startUrl},testcaseId, Map.of(),
                    "gotoUrl", "", epic, feature,story, description
            ));
            logger.info("\uD83C\uDF10 Recording session started → {}", startUrl);
            logger.info("Close the browser window when finished");
            // Main monitoring loop - ends when browser/page is closed
            keepPollingAndSyncActions(page, testcaseId);
            // When we reach here → browser was closed
            logger.info("\n🔒 Browser closed → saving recorded actions...");
            saveActionsToJson(outputJsonPath);

        } catch (Exception e) {
            System.err.println("Session error: " + e.getMessage());
        }
    }

    private void keepPollingAndSyncActions(Page page, String testcaseId) {
        long lastSuccessfulPoll = System.currentTimeMillis();
        while (true) {
            try {
                page.evaluate("true");           // cheap alive check
                syncActionsFromPage(page, testcaseId);       // read array
                lastSuccessfulPoll = System.currentTimeMillis();
                Thread.sleep(800);               // 0.8–1.2 s is usually fine
            } catch (PlaywrightException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("execution context was destroyed") ||
                        msg.contains("navigation") ||
                        msg.contains("target closed")) {
                    logger.warn("→ Navigation / context destroy detected — waiting a bit ...");
                    // Give browser 4–8 seconds to finish navigation
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                    // Try one more sync — new context may be alive
                    try {
                        syncActionsFromPage(page, testcaseId);
                        logger.info("→ Recovered after navigation");
                        continue;
                    } catch (Exception ignored) {}
                    // If still failing → check if page is really gone
                    if (System.currentTimeMillis() - lastSuccessfulPoll > 15000) { // 15 seconds no response
                        logger.warn("→ No life signs for 15s → assuming session ended");
                        break;
                    }
                } else {
                    // Other serious exception → probably real close or crash
                    logger.error("Unexpected Playwright error: {}", String.valueOf(e));
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void syncActionsFromPage(Page page, String testcaseId) {
        try {
//            Object result = page.evaluate("""
//                () => {
//                    const acts = window.recordedActions || [];
//                    // Optional: clear after sync to avoid huge arrays (comment if unwanted)
//                    window.recordedActions = [];
//                    return acts;
//                }
//                """);
            ObjectMapper mapper = new ObjectMapper();
            Object result = page.evaluate("""
                () => {
                    let acts = [];
                    try {
                        const stored = localStorage.getItem('recordedActions');
                        if (stored) {
                            acts = JSON.parse(stored);
                        }
                    } catch (e) {
                        console.error('Failed to read localStorage:', e);
                    }
                    // Optional: clear after sync (uncomment if you want to reset)
                    localStorage.removeItem('recordedActions');
                    window.recordedActions = [];
                    return acts;
                }
            """);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> freshActions = (List<Map<String, Object>>) result;
            List<ActionRecord> mappedActions = mapper.convertValue(
                    freshActions,
                    new TypeReference<List<ActionRecord>>() {}
            ).stream()
            .map(a -> a.withTestcaseId(testcaseId))
            .toList();
            if (!mappedActions.isEmpty()) {
                actions.addAll(mappedActions);
//                double lastTs = actions.isEmpty() ? 0 : (double) actions.getLast().get("timestamp");
//                for (ActionRecord act : actions) {
//                    double ts = ((Number) act.get("timestamp")).doubleValue();
//                    if (ts > lastTs) {
//                    actions.add(act);
//                        lastTs = ts;
//                    }
//                }
                logger.info("⏱️ Synced → total actions: {} %n", actions.size());
            }
        } catch (Exception e) {
            // Silent fail during polling - usually page closing
            logger.error("syncActionsFromPage exception - {}", e.getMessage());
        }
    }

    private void injectRecorderScript(Page page) {
        page.addInitScript("""
            // Load existing actions from localStorage (survives same-origin navigation)
//            let recordedActions = [];
            window.recordedActions = window.recordedActions || [];
            try {
                const stored = localStorage.getItem('recordedActions');
                if (stored) {
                    window.recordedActions = JSON.parse(stored);
                }
            } catch (e) {
                console.error('Failed to load window.recordedActions:', e);
            }

            let typingBuffer = {};
            let lastScrollTime = 0;
            let lastScrollY = window.scrollY;

            function getRobustSelector(el) {
                if (!el || el === document.body) return 'body';
                if (el.id) return '#' + el.id;
                let parts = [];
                let current = el;
                let depth = 0;
                while (current && current !== document.body && depth < 5) {
                    let tag = current.tagName.toLowerCase();
                    let sel = tag;
                    if (current.className && typeof current.className === 'string') {
                        let classes = current.className.trim().split(/\\s+/);
                        if (classes.length > 0) sel += '.' + classes[0];
                    }
                    if (current.parentElement) {
                        let siblings = Array.from(current.parentElement.children)
                            .filter(c => c.tagName === current.tagName);
                        if (siblings.length > 1) {
                            let idx = siblings.indexOf(current);
                            if (idx >= 0) sel += `:nth-of-type(${idx + 1})`;
                        }
                    }
                    parts.unshift(sel);
                    current = current.parentElement;
                    depth++;
                }
                return parts.join(' > ');
            }

            function saveActions() {
                try {
                    localStorage.setItem('recordedActions', JSON.stringify(window.recordedActions));
                } catch (e) {
                    console.error('Failed to save to localStorage:', e);
                }
            }

            // CLICK
            document.addEventListener('click', e => {
                if (e.target.closest('button,a,input,textarea,select,label,[role="button"]')) {
                    const selector = getRobustSelector(e.target);
                    const action = {
                        actionType: 'click',
                        locator: selector,
                        arguments: [ ],
                        methodName: 'click'
                    };
                    window.recordedActions.push(action);
                    console.log("ACTION:" + JSON.stringify(action));
                    saveActions();
                }
            }, true);

            // INPUT / TYPING (debounced)
            document.addEventListener('input', e => {
                const t = e.target;
                if (t.matches('input, textarea')) {
                    const selector = getRobustSelector(t);
                    typingBuffer[selector] = t.value;

                    if (window.typeTimeout) clearTimeout(window.typeTimeout);
                    window.typeTimeout = setTimeout(() => {
                        const action = {
                            actionType: 'type',
                            locator: selector,
                            arguments: [typingBuffer[selector]],
                            methodName: 'type'
                        };
                        window.recordedActions.push(action);
                        console.log("ACTION:" + JSON.stringify(action));
                        saveActions();
                        delete typingBuffer[selector];
                    }, 700);
                }
            }, true);

            // KEYPRESS
            document.addEventListener('keydown', e => {
                if (!['Enter','Tab','ArrowUp','ArrowDown','ArrowLeft','ArrowRight','Escape'].includes(e.key)) return;
                const active = document.activeElement;
                if (!active || active === document.body) return;
                const selector = getRobustSelector(active);
                const action = {
                    actionType: 'keypress',
                    locator: selector,
                    arguments: [e.key],
                    methodName: 'keypress'
                };
                window.recordedActions.push(action);
                console.log("ACTION:" + JSON.stringify(action));
                saveActions();
            }, true);

            // SCROLL - debounced
            window.addEventListener('scroll', () => {
                const now = Date.now();
                if (now - lastScrollTime < 400) return;
                if (Math.abs(window.scrollY - lastScrollY) < 30) return;

                lastScrollTime = now;
                lastScrollY = window.scrollY;

                const action = {
                    actionType: 'scroll',
                    locator: '',
                    arguments: [ window.scrollX, window.scrollY ],
                    methodName: 'scroll'
                };
//                window.recordedActions.push(action);
                console.log("ACTION:" + JSON.stringify(action));
//                saveActions();
            }, { passive: true });

            console.log('🎥 Action recorder active (using localStorage) — start interacting');
        """);
    }

    private void saveActionsToJson(String path) {
        try {
            Files.createDirectories(Paths.get(path).getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), actions);
            logger.info("✅ Saved {} actions to: {} %n", actions.size(), path);
        } catch (Exception e) {
            logger.error("Failed to save JSON: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
//        new ActionRecorder().recordSession(
//                "https://www.selenium.dev/selenium/web/fedcm/signin.html",
//                "output/recorded-actions.json"
//        );
    }
}

/*
//        try {
//            while (true) {
//                page.evaluate("true");
//                syncActionsFromPage(page);
//                Thread.sleep(950); // ≈1s polling
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } catch (PlaywrightException e) {
//            // This is expected when browser/context/page is closed
//            System.out.println("Playwright detected browser/page closure");
//            syncActionsFromPage(page);
//        }
 */