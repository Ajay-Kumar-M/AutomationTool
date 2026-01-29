package org.automation.executor;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.automation.driver.PlaywrightRecorder;

import java.util.concurrent.CountDownLatch;

public class RecorderRunner {
    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

//            Page page = browser.newPage();

            PlaywrightRecorder recorder = new PlaywrightRecorder(browser);

            // When tab is closed → unblock main thread
//            page.onClose(p -> {
//                System.out.println("page onclose rr called");
//                recorder.saveOnce();
//                latch.countDown();
//            });
            browser.onDisconnected(b-> {
                System.out.println("browser onclose rr called");
                recorder.saveOnce();
                latch.countDown();
            });

//            page.navigate("https://www.selenium.dev/selenium/web/fedcm/signin.html");
//
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//
//            page.evaluate("""
//                () => {
//                    const getSelector = (el) => {
//                        if (el.id) return '#' + el.id;
//                        let path = [];
//                        while (el && el.nodeType === Node.ELEMENT_NODE) {
//                            let sel = el.nodeName.toLowerCase();
//                            if (el.className) sel += '.' + el.className.trim().split(' ').join('.');
//                            path.unshift(sel);
//                            el = el.parentElement;
//                        }
//                        return path.join(' > ');
//                    };
//
//                    // Direct listener attachment with binding guarantee
//                    document.addEventListener('click', (e) => {
//                        console.log('click event', getSelector(e.target));
//                        window.recordAction('click', getSelector(e.target));
//                    });
//
//                    document.addEventListener('input', (e) => {
//                        if (e.target.value !== undefined) {
//                            console.log('input event', getSelector(e.target), e.target.value);
//                            window.recordAction('type', getSelector(e.target), e.target.value);
//                        }
//                    });
//
//                    window.addEventListener('keydown', (e) => {
//                        if (e.ctrlKey && e.key.toLowerCase() === 'q') {
//                            window.recordAction('STOP');
//                        }
//                    });
//
//                    console.log('Listeners attached via evaluate');
//                }
//                """);

            // ✅ BLOCK until browser tab is closed
            latch.await();
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        System.out.println("Shutdown hook saving recording");
                        recorder.saveOnce();
                    })
            );
        } catch (InterruptedException e) {
            System.out.println("exception called");
            throw new RuntimeException(e);
        }
    }
}

