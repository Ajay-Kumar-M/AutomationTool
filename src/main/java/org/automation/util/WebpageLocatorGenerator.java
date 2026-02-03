package org.automation.util;

import org.automation.records.LocatorData;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebpageLocatorGenerator {
    private WebDriver driver;

    public static void main(String[] args) {
        WebpageLocatorGenerator generator = new WebpageLocatorGenerator();
        generator.generateLocators("https://www.google.com/");
    }

//    public void generate(String url){
//        WebpageLocatorGenerator generator = new WebpageLocatorGenerator();
//        generator.setupDriver();
//        generator.generateLocators(url);
//        generator.driver.quit();
//    }

    private void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
    }

    public List<LocatorData> generateLocators(String url) {
        setupDriver();
        driver.get(url);
        List<WebElement> interactableElements = findAllInteractableElements();
        List<LocatorData> allLocatorData = new ArrayList<>(List.of());

        for (int i = 0; i < interactableElements.size(); i++) {
            WebElement element = interactableElements.get(i);
            LocatorData locators = generateUniqueLocators(element);
            allLocatorData.add(locators);
        }
        driver.quit();
        return allLocatorData;
    }

    private List<WebElement> findAllInteractableElements() {
        // Common interactable selectors (buttons, inputs, links, selects, etc.)
        List<By> selectors = Arrays.asList(
                By.tagName("button"),
                By.tagName("input"),
                By.tagName("a"),
                By.tagName("select"),
                By.cssSelector("[role='button']"),
                By.cssSelector("textarea"),
                By.cssSelector("[onclick]"),
                By.cssSelector("[data-testid]")
        );

        return selectors.stream()
                .flatMap(by -> {
                    try {
                        return driver.findElements(by).stream();
                    } catch (StaleElementReferenceException e) {
                        return Stream.empty();
                    }
                })
                .filter(this::isInteractable)
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
    }

    private boolean isInteractable(WebElement element) {
        try {
            String tag = element.getTagName();
            // Skip hidden/disabled elements
            if (!element.isDisplayed() || !element.isEnabled()) return false;
            // Skip non-interactable tags
            if (tag.equals("script") || tag.equals("style")) return false;

            // Check if actually clickable/focusable
            String style = element.getAttribute("style");
            if (style != null && (style.contains("display:none") || style.contains("visibility:hidden"))) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private LocatorData generateUniqueLocators(WebElement element) {
        String css = generateRelativeCSS(element);
        String xpath = generateRelativeXPath(element);
        return new LocatorData(element.getTagName().toUpperCase(), getElementText(element), css, xpath);
    }

    public String generateRelativeCSS(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String css = (String) js.executeScript("""
            function generateCssPath(el) {
                if (!(el instanceof Element)) return '';
                
                const path = [];
                let node = el;
                
                while (node && node.nodeType === Node.ELEMENT_NODE) {
                    let selector = node.tagName.toLowerCase();
                    
                    // Strongest signal first — ID (almost always unique)
                    if (node.id) {
                        // Check if ID is really unique on the page
                        if (document.querySelectorAll('#' + CSS.escape(node.id)).length === 1) {
                            return '#' + CSS.escape(node.id);
                        }
                        selector = '#' + CSS.escape(node.id);
                    }
                    
                    // Classes — take the first "semantic" one (or most specific if you have heuristics)
                    const classes = Array.from(node.classList)
                        .filter(cls => cls && !/\\d+$/.test(cls))   // skip pure numeric tail classes
                        .map(cls => CSS.escape(cls));
                    
                    if (classes.length > 0) {
                        selector += '.' + classes[0];   // ← most projects take first class
                        // Alternative: take all → '.' + classes.join('.')
                    }
                    
                    // Positional index only when necessary
                    const parent = node.parentElement;
                    if (parent) {
                        const sameTagSiblings = Array.from(parent.children)
                            .filter(child => child.tagName === node.tagName);
                        
                        if (sameTagSiblings.length > 1) {
                            const index = sameTagSiblings.indexOf(node) + 1;
                            selector += `:nth-of-type(${index})`;
                        }
                    }
                    
                    path.unshift(selector);
                    node = parent;
                    
                    // Prevent very long / brittle paths
                    if (path.length >= 5) break;
                }
                
                return path.join(' > ');
            }
            
            return generateCssPath(arguments[0]);
            """, element);

        if (css.isEmpty()) return fallbackCSS(element);
        if (isUnique("cssSelector", css)) return css;

        // Try shorter version without last positional index
        String shorter = css.replaceAll(":nth-of-type\\(\\d+\\)$", "");
        if (!shorter.isEmpty() && isUnique("cssSelector", shorter)) {
            return shorter;
        }

        return fallbackCSS(element);
    }

    private String generateAbsoluteXPath(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String xpath = (String) js.executeScript(
                "function getXPath(el) {" +
                        "  if (!(el instanceof Element)) return '';" +
                        "  const path = [];" +
                        "  let current = el;" +
                        "  while (current && current.nodeType === Node.ELEMENT_NODE) {" +
                        "    let index = 1;" +
                        "    const siblings = Array.from(current.parentNode ? current.parentNode.children : []);" +
                        "    for (let sib of siblings) {" +
                        "      if (sib.nodeType === Node.ELEMENT_NODE && sib.tagName === current.tagName) {" +
                        "        if (sib === current) break;" +
                        "        index++;" +
                        "      }" +
                        "    }" +
                        "    let xpathPart = current.tagName.toLowerCase();" +
                        "    if (siblings.filter(sib => sib.tagName === current.tagName).length > 1) {" +
                        "      xpathPart += '[' + index + ']';" +
                        "    }" +
                        "    const id = current.id ? \"[@id='\" + current.id.replace(/'/g, \"&apos;\") + \"']\" : '';" +
                        "    if (id) xpathPart += id;" +
                        "    else if (current.className) {" +
                        "      xpathPart += \"[contains(@class, '\" + current.className.trim().split(' ')[0].replace(/'/g, \"&apos;\") + \"')]\";" +
                        "    }" +
                        "    path.unshift(xpathPart);" +
                        "    current = current.parentNode;" +
                        "    if (path.length > 6) break;" +
                        "  }" +
                        "  return '//' + path.join('/');" +
                        "}" +
                        "return getXPath(arguments[0]);", element);

        return xpath;
    }

    public String generateRelativeXPath(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String xpath = (String) js.executeScript("""
                function generateXPath(el) {
                    if (!(el instanceof Element)) return '';
                
                    const parts = [];
                    let node = el;
                
                    while (node && node.nodeType === Node.ELEMENT_NODE) {
                        let part = node.tagName.toLowerCase();
                
                        // ID — strongest anchor
                        if (node.id) {
                            const safeId = node.id.replace(/"/g, '&quot;');
                            const expr = `//*[@id="${safeId}"]`;
                
                            // Check if this ID is truly unique and points to this element
                            let iterator;
                            try {
                                iterator = document.evaluate(expr, document, null, XPathResult.ANY_TYPE, null).iterateNext();
                            } catch (e) {
                                iterator = null; // invalid → treat as non-unique
                            }
                
                            if (iterator === node) {
                                return expr;
                            }
                
                            part += `[@id="${safeId}"]`;
                        }
                        // First class as fallback anchor
                        else if (node.className && node.className.trim()) {
                            const firstClass = node.className.trim().split(/\\s+/)[0];
                            const safeClass = firstClass.replace(/"/g, '&quot;');
                            part += `[contains(concat(' ', @class, ' '), ' ${safeClass} ')]`;
                        }
                
                        // Index only when needed
                        const parent = node.parentElement;
                        if (parent) {
                            let index = 1;
                            for (let sib of parent.children) {
                                if (sib === node) break;
                                if (sib.tagName === node.tagName) index++;
                            }
                            const sameTagCount = Array.from(parent.children)
                                .filter(c => c.tagName === node.tagName).length;
                
                            if (sameTagCount > 1) {
                                part += `[${index}]`;
                            }
                        }
                
                        parts.unshift(part);
                        node = parent;
                
                        if (parts.length >= 6) break;
                    }
                
                    return '/' + parts.join('/');
                }
                
                return generateXPath(arguments[0]);
            """, element);

        if (xpath.isEmpty() || xpath.equals("/")) {
            xpath = fallbackXPath(element);
        }
        if (isUnique("xpath", xpath)) return xpath;
        String fallbackXPath = fallbackXPath(element);
        if (isUnique("xpath", fallbackXPath)) return fallbackXPath;
        return generateAbsoluteXPath(element);
    }

    private boolean isUnique(String strategy, String selector) {
        try {
            long count = switch (strategy) {
                case "cssSelector" -> driver.findElements(By.cssSelector(selector)).size();
                case "xpath" -> driver.findElements(By.xpath(selector)).size();
                default -> 0L;
            };
            return count == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private String fallbackCSS(WebElement el) {
        try {
            String id = el.getAttribute("id");
            if (id != null && !id.isBlank()) {
                return "#" + id;  // CSS.escape not needed here — Selenium handles it
            }
            String tag = el.getTagName().toLowerCase();
            String cls = el.getAttribute("class");
            if (cls != null && !cls.isBlank()) {
                String firstCls = cls.trim().split("\\s+")[0];
                return tag + "." + firstCls;
            }
            return tag;
        } catch (Exception e) {
            return el.getTagName().toLowerCase();
        }
    }

    private String fallbackXPath(WebElement el) {
        try {
            String id = el.getAttribute("id");
            if (id != null && !id.isBlank()) {
                return "//*[@id='" + id.replace("'", "&apos;") + "']";
            }
            String tag = el.getTagName().toLowerCase();
            String cls = el.getAttribute("class");
            if (cls != null && !cls.isBlank()) {
                String first = cls.trim().split("\\s+")[0];
                return "//" + tag + "[contains(@class,'" + first.replace("'", "&apos;") + "')]";
            }
            return "//" + tag;
        } catch (Exception e) {
            return "//" + el.getTagName().toLowerCase();
        }
    }

    private String getElementText(WebElement element) {
        try {
            // 1. Visible text
            String text = element.getText();
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            // 2. aria-label
            text = element.getAttribute("aria-label");
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            // 3. Associated <label for="id">
            String id = element.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                List<WebElement> labels = driver.findElements(
                        By.xpath("//label[@for='" + id + "']")
                );
                if (!labels.isEmpty()) {
                    return labels.get(0).getText().trim();
                }
            }

            // 4. Parent <label>
            try {
                WebElement parentLabel = element.findElement(By.xpath("ancestor::label"));
                text = parentLabel.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            } catch (Exception ignored) {}

            // 5. placeholder
            text = element.getAttribute("placeholder");
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            // 6. title
            text = element.getAttribute("title");
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            // 7. value (for buttons & inputs)
            text = element.getAttribute("value");
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

}