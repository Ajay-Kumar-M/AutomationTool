import org.automation.driver.BrowserConfig;
import org.automation.driver.Driver;
import org.testng.annotations.Test;

public class TestLogin {
    @Test
    public void loginTest() {
        Driver browser = BrowserConfig.getBrowserActions();
        // ... test code
    }

}
