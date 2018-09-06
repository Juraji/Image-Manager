package nl.juraji.imagemanager.util.io.web.drivers;

import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Juraji on 21-6-2018.
 * Pinterest Downloader
 */
public class WebDriverPool extends GenericObjectPool<RemoteWebDriver> {
    private static final AtomicObject<WebDriverPool> INSTANCE = new AtomicObject<>();
    private final Logger logger;

    private WebDriverPool() {
        super(new ChromeDriverFactory());
        this.logger = Log.create(this);
        this.setBlockWhenExhausted(true);
        this.setMaxTotal(1);
        this.setTestOnBorrow(true);
        this.setTestOnReturn(true);
        this.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(1));
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
        logger.warn("Driver instance manually invalidated: " + driver.getSessionId());
        logBrowserLogs(driver);
        super.invalidateObject(driver);
    }

    private void logBrowserLogs(RemoteWebDriver driver) {
        if (Preferences.Application.isDebugMode()) {
            try {
                final LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

                logEntries.forEach(logEntry -> {
                    final Level level = logEntry.getLevel();
                    if (Level.SEVERE.equals(level)) {
                        logger.error(logEntry.getMessage());
                    } else if (Level.WARNING.equals(level)) {
                        logger.warn(logEntry.getMessage());
                    } else {
                        logger.info(logEntry.getMessage());
                    }
                });
            } catch (WebDriverException e) {
                logger.error("Failed retrieving browser logs", e);
            }
        }
    }
}
