package org.automation;

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
        options.addArguments("--headless"); // Optional: run headless
        driver = new ChromeDriver(options);
    }

    public List<LocatorData> generateLocators(String url) {
        setupDriver();
        driver.get(url);
        List<WebElement> interactableElements = findAllInteractableElements();
        List<LocatorData> allLocatorData = new ArrayList<>(List.of());

        System.out.println("Found " + interactableElements.size() + " interactable elements:");
        System.out.println("═".repeat(80));

        for (int i = 0; i < interactableElements.size(); i++) {
            WebElement element = interactableElements.get(i);
            LocatorData locators = generateUniqueLocators(element);
            System.out.printf("\n[%d] %s%n", i + 1, locators.tagName());
            System.out.println("   CSS: " + locators.css());
            System.out.println("   XPath: " + locators.xpath());
            System.out.println("   Text: " + locators.text());
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
        String css = generateRelativeCSS3(element);
        String xpath = generateRelativeXPath4(element);
        return new LocatorData(element.getTagName().toUpperCase(), getElementText(element), css, xpath);
    }

    private String generateRelativeCSS2(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String cssPath = (String) js.executeScript(
                "function getCSSPath(el) {" +
                        "  if (!(el instanceof Element)) return '';" +
                        "  const path = [];" +
                        "  let current = el;" +
                        "  while (current && current.nodeType === Node.ELEMENT_NODE) {" +
                        "    let selector = current.nodeName.toLowerCase();" +
                        "    if (current.id && document.getElementById(current.id)) {" +
                        "      return '#' + CSS.escape(current.id);" +
                        "    }" +
                        "    const siblings = Array.from(current.parentNode ? current.parentNode.children : []);" +
                        "    const sameTagSiblings = siblings.filter(sib => sib.nodeName === current.nodeName);" +
                        "    const index = sameTagSiblings.indexOf(current) + 1;" +
                        "    if (sameTagSiblings.length > 1) {" +
                        "      selector += ':nth-of-type(' + index + ');';" +
                        "    }" +
                        "    const classes = current.className ? current.className.trim().split(/\\s+/) : [];" +
                        "    if (classes.length) {" +
                        "      selector += '.' + classes[0].replace(/[^a-zA-Z0-9_-]/g, '\\\\$&');" +
                        "    }" +
                        "    path.unshift(selector);" +
                        "    current = current.parentNode;" +
                        "    if (path.length > 6) break;" +
                        "  }" +
                        "  return path.join(' > ');" +
                        "}" +
                        "return getCSSPath(arguments[0]);", element);

        return isUnique("cssSelector", cssPath) ? cssPath : fallbackCSS(element);
    }

    public String generateRelativeCSS3(WebElement element) {
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


        System.out.println("Xpath - "+xpath);
        System.out.println("is unique Xpath - "+isUnique("xpath", xpath));
        return isUnique("xpath", xpath) ? xpath : fallbackXPath(element);
    }

    public String generateRelativeXPath4(WebElement element) {
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
        System.out.println("fallbackxpath called "+el.getTagName());
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

    private boolean isUniqueSelector(String strategy, String selector) {
        try {
            List<WebElement> elements = switch (strategy) {
                case "cssSelector" -> driver.findElements(By.cssSelector(selector));
                case "xpath" -> driver.findElements(By.xpath(selector));
                default -> List.of();
            };
            return elements.size() == 1;
        } catch (Exception e) {
            return false;
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

/*
    private String generateRelativeXPath2(WebElement element) {
        try {
            // JavaScript function to generate unique relative XPath
            String jsScript = "function getXPath(element) {" +
                    "    if (element.id !== '') {" +
                    "        return 'id(\"' + element.id + '\")';" +
                    "    }" +
                    "    if (element === document.body) {" +
                    "        return element.tagName.toLowerCase();" +
                    "    }" +
                    "    var ix = 0;" +
                    "    var siblings = element.parentNode.childNodes;" +
                    "    for (var i = 0; i < siblings.length; i++) {" +
                    "        var sibling = siblings[i];" +
                    "        if (sibling === element) {" +
                    "            return getXPath(element.parentNode) + '/' + element.tagName.toLowerCase() + '[' + (ix + 1) + ']';" +
                    "        }" +
                    "        if (sibling.nodeType === 1 && sibling.tagName === element.tagName) {" +
                    "            ix++;" +
                    "        }" +
                    "    }" +
                    "};" +
                    "return getXPath(arguments[0]);";

            JavascriptExecutor js = (JavascriptExecutor) driver;
            String xpath = (String) js.executeScript(jsScript, element);
            System.out.println("Element: " + element.getTagName() + " - XPath: " + xpath);
            // Clean up and verify
//            if (xpath.startsWith("//")) {
//                xpath = xpath.replaceAll("//+", "//"); // Normalize
//                System.out.println("Xpath - "+xpath);
//                System.out.println("is unique Xpath - "+isUniqueSelector("xpath", xpath));
//                if (isUniqueSelector("xpath", xpath)) {
//                    return xpath;
//                }
//            }
            System.out.println("Xpath - "+xpath);
            System.out.println("is unique Xpath - "+isUniqueSelector("xpath", xpath));
            return xpath;
        } catch (Exception e) {
            // Fallback to basic relative
            System.out.println("Xpath exception occurred");
            return "//" + element.getTagName().toLowerCase() +
                    "[contains(@class,'" + element.getAttribute("class") + "')]";
        }
//        return "XPath generation failed";
    }

    private String fallbackXPathOld(WebElement el) {
        try {
            String id = el.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                return "//*[@id='" + id + "']";
            }
            return "//" + el.getTagName().toLowerCase() +
                    "[contains(@class,'" + el.getAttribute("class") + "')]";
        } catch (Exception e) {
            return "//" + el.getTagName().toLowerCase();
        }
    }
 */
/*
    private String generateRelativeCSS(WebElement element) {
        try {
            // Use JavaScript to generate shortest unique CSS selector
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String jsScript = """
                function getUniqueCSSPath(el) {
                    if (!(el instanceof Element)) return '';
                    const path = [];
                    while (el && el.nodeType === Node.ELEMENT_NODE) {
                        let selector = el.nodeName.toLowerCase();
                        if (el.id && document.getElementById(el.id)) {
                            return '#' + el.id;
                        }
                        if (el.className && typeof el.className === 'string') {
                            selector += '.' + el.className.trim().replace(/\\s+/g, '.');
                        }
                        let index = Array.from(el.parentNode.children).indexOf(el) + 1;
                        if (el.parentNode.children.length > 1) {
                            selector += ':nth-child(' + index + ')';
                        }
                        path.unshift(selector);
                        el = el.parentNode;
                        if (path.length > 5) break; // Limit depth
                    }
                    return path.join(' > ');
                }
                return getUniqueCSSPath(arguments[0]);
                """;

            Object result = js.executeScript(jsScript, element);
            String cssPath = String.valueOf(result);

            // Verify uniqueness
            if (isUniqueSelector("cssSelector", cssPath)) {
                return cssPath;
            }
        } catch (Exception e) {
            // Fallback
        }
        return "CSS generation failed";
    }

    private String fallbackCSSOld(WebElement el) {
        try {
            return el.getTagName().toLowerCase() +
                    "[@id='" + el.getAttribute("id") + "']";
        } catch (Exception e) {
            return el.getTagName().toLowerCase();
        }
    }
 */