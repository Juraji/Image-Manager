package nl.juraji.imagemanager.util.io.web.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public abstract class WebDriverFactory extends BasePooledObjectFactory<RemoteWebDriver> {

    protected WebDriverFactory(WebDriverManager webDriverManager) {
        webDriverManager.targetPath("./");
        webDriverManager.setup();
    }

    @Override
    public PooledObject<RemoteWebDriver> wrap(RemoteWebDriver driver) {
        return new DefaultPooledObject<>(driver);
    }

    @Override
    public void destroyObject(PooledObject<RemoteWebDriver> p) {
        p.getObject().quit();
    }

    @Override
    public boolean validateObject(PooledObject<RemoteWebDriver> p) {
        try {
            // Try getting the current url, if that fails the driver instance is broken
            final String currentUrl = p.getObject().getCurrentUrl();
            return !StringUtils.isEmpty(currentUrl);
        } catch (WebDriverException e) {
            return false;
        }
    }

    @Override
    public RemoteWebDriver create() {
        return create(getCapabilities());
    }

    public abstract RemoteWebDriver create(Capabilities capabilities);

    public abstract Capabilities getCapabilities();
}
