package nl.juraji.imagemanager.util.io;

import io.github.bonigarcia.wdm.WebDriverManager;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 21-6-2018.
 * Pinterest Downloader
 */
public class WebDriverPool extends GenericObjectPool<RemoteWebDriver> {
    private static final AtomicObject<WebDriverPool> INSTANCE = new AtomicObject<>();
    private final Logger logger;

    private WebDriverPool() {
        super(new PoolabeWebDriverFactory());
        this.logger = Logger.getLogger(getClass().getName());
        this.setBlockWhenExhausted(true);
        this.setMaxTotal(1);
        this.setTestOnBorrow(true);
        this.setTestOnReturn(true);
    }

    public static RemoteWebDriver getDriver() throws Exception {
        // Synchronization guarantees thread safety when creating the driver pool
        synchronized (WebDriverPool.class) {
            if (INSTANCE.isEmpty()) {
                final WebDriverPool pool = new WebDriverPool();
                pool.preparePool();
                INSTANCE.set(pool);
            }
        }

        return INSTANCE.get().borrowObject();
    }

    public static void returnDriver(RemoteWebDriver driver) {
        if (INSTANCE.isSet()) {
            INSTANCE.get().returnObject(driver);
        }
    }

    public static void shutdown() {
        if (INSTANCE.isSet()) {
            INSTANCE.get().close();
        }
    }

    public static void invalidateDriver(RemoteWebDriver driver) throws Exception {
        if (INSTANCE.isSet()) {
            INSTANCE.get().invalidateObject(driver);
        }
    }

    @Override
    public void returnObject(RemoteWebDriver driver) {
        logger.info("Driver instance returned: " + driver.getSessionId());
        logBrowserLogs(driver);
        super.returnObject(driver);
    }

    @Override
    public RemoteWebDriver borrowObject() throws Exception {
        logger.info("Driver instance requested");
        final RemoteWebDriver driver = super.borrowObject();
        logger.info("Driver instance available: " + driver.getSessionId());
        return driver;
    }

    @Override
    public void close() {
        logger.info("Close requested, quitting existing driver instances");
        super.close();
    }

    @Override
    public void invalidateObject(RemoteWebDriver driver) throws Exception {
        logger.log(Level.WARNING, "Driver instance manually invalidated: " + driver.getSessionId());
        logBrowserLogs(driver);
        super.invalidateObject(driver);
    }

    private void logBrowserLogs(RemoteWebDriver driver) {
        if (Preferences.isDebugMode()) {
            try {
                final LogEntries logEntries = driver.manage().logs().get("browser");

                logEntries.forEach(logEntry -> {
                    final Level level = logEntry.getLevel();
                    if (Level.SEVERE.equals(level)) {
                        logger.log(Level.SEVERE, logEntry.getMessage());
                    } else if (Level.WARNING.equals(level)) {
                        logger.log(Level.WARNING, logEntry.getMessage());
                    } else {
                        logger.info(logEntry.getMessage());
                    }
                });
            } catch (WebDriverException e) {
                logger.log(Level.SEVERE, "Failed retrieving browser logs", e);
            }
        }
    }

    private static class PoolabeWebDriverFactory extends BasePooledObjectFactory<RemoteWebDriver> {
        private final ChromeOptions driverOptions;

        public PoolabeWebDriverFactory() {
            WebDriverManager driverManager = WebDriverManager.chromedriver();
            driverManager.targetPath("./");
            driverManager.setup();

            driverOptions = new ChromeOptions();
            driverOptions.addArguments("--window-size=1366,768");
            driverOptions.setHeadless(!Preferences.isDebugMode());
        }

        @Override
        public RemoteWebDriver create() {
            return new ChromeDriver(driverOptions);
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
    }
}
