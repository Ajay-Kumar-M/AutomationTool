import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.automation.driver.PlaywrightDriver;
import org.automation.driver.SeleniumDriver;
import org.automation.util.JsonScriptRunner;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureListener.class})
public class TestLogin {
    Driver browser;
    @BeforeMethod
    public void setUp(ITestContext context) {  // ✓ Add parameter
        browser = BrowserConfig.getBrowserActions();
        if (browser instanceof SeleniumDriver) {
            context.setAttribute("isWebdriver", true);
            context.setAttribute("webdriver", ((SeleniumDriver) browser).getDriver());
        } else if (browser instanceof PlaywrightDriver) {
            context.setAttribute("isWebdriver", false);
            context.setAttribute("pagedriver", ((PlaywrightDriver) browser).getPage());
            // obj is ClassB
        }
    }

    @Test
    public void loginTest(ITestContext context) throws Exception {  // ✓ Add parameter
        JsonScriptRunner runner = new JsonScriptRunner();
        context.setAttribute("testcaseID", "TC002");
        runner.run(browser,"src/main/java/org/automation/data/TC002.json");
        context.setAttribute("testcaseID", "TC001");
        runner.run(browser,"src/main/java/org/automation/data/TC001.json");
    }

    @AfterMethod
    public void tearDown(ITestContext context) {
        browser.close();
    }
}
