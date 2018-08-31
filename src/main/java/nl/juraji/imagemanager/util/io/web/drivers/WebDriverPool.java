package nl.juraji.imagemanager.util.io.web.drivers;

import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.TimeUnit;
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
        super(new ChromeDriverFactory());
        this.logger = Logger.getLogger(getClass().getName());
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
        logger.log(Level.WARNING, "Driver instance manually invalidated: " + driver.getSessionId());
        logBrowserLogs(driver);
        super.invalidateObject(driver);
    }

    private void logBrowserLogs(RemoteWebDriver driver) {
        if (Preferences.isDebugMode()) {
            try {
                final LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

                logEntries.forEach(logEntry -> {
                    final Level level = logEntry.getLevel();
                    if (Level.SEVERE.equals(level)) {
                        logger.log(Level.SEVERE, logEntry.getMessage());
                    } else if (Level.WARNING.equals(level)) {
                        logger.log(Level.WARNING, logEntry.getMessage());
                    } else {
                        logger.log(Level.INFO, logEntry.getMessage());
                    }
                });
            } catch (WebDriverException e) {
                logger.log(Level.SEVERE, "Failed retrieving browser logs", e);
            }
        }
    }
}