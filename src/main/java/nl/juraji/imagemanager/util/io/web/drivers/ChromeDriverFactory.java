package nl.juraji.imagemanager.util.io.web.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import nl.juraji.imagemanager.util.Preferences;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public final class ChromeDriverFactory extends WebDriverFactory {

    public ChromeDriverFactory() {
        super(WebDriverManager.chromedriver());
    }

    @Override
    public RemoteWebDriver create(Capabilities capabilities) {
        return new ChromeDriver((ChromeOptions) capabilities);
    }

    @Override
    public Capabilities getCapabilities() {
        ChromeOptions driverOptions = new ChromeOptions();
        driverOptions.addArguments("--window-size=1366,768");
        driverOptions.setHeadless(!Preferences.isDebugMode());
        return driverOptions;
    }
}
