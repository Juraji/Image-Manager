package nl.juraji.imagemanager.util.io.pinterest;

import io.github.bonigarcia.wdm.WebDriverManager;
import nl.juraji.imagemanager.util.io.web.drivers.WebDriverPool;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.PropertyKey;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class PinterestWebSession implements AutoCloseable {
    private static final String SCRAPER_DATA_BUNDLE_NAME = "nl.juraji.imagemanager.util.io.pinterest.scraper-data";

    static {
        final WebDriverManager manager = WebDriverManager.chromedriver();
        manager.targetPath("./");
        manager.setup();
    }

    private final String username;
    private final String password;
    private final ResourceBundle scraperData;
    private final CookieJar cookieJar;
    private RemoteWebDriver driver;

    public PinterestWebSession(String username, String password) {
        this.username = username;
        this.password = password;
        this.scraperData = ResourceBundle.getBundle(SCRAPER_DATA_BUNDLE_NAME);
        this.cookieJar = getCookieJar();

        try {
            this.driver = WebDriverPool.getDriver();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CookieJar getCookieJar() {
        return new CookieJar("pinterest.com");
    }

    @Override
    public void close() {
        if (driver != null) {
            WebDriverPool.returnDriver(driver);
            driver = null;
        }
    }

    /**
     * Navigate to the user's profile
     */
    public void goToProfile() throws Exception {
        String urlUsername = username.split("@")[0];
        navigate(getData("data.urls.pinterest.main") + urlUsername);
    }

    /**
     * Navigate to the given url
     */
    public void navigate(String uri) throws Exception {
        if (!driver.getCurrentUrl().equals(uri)) {
            driver.get(uri);
        }

        checkDoLogin();
    }

    /**
     * Scroll to the end of the current page
     * Scroll to the end of the current page
     *
     * @throws Exception Driver error oir script error
     */
    public void scrollDown(long wait) throws Exception {
        executeScript("/nl/juraji/imagemanager/util/io/pinterest/js/window-scroll-down.js");
        Thread.sleep(wait);
    }

    public WebElement getElement(By by) {
        return await(ExpectedConditions.presenceOfElementLocated(by));
    }

    public List<WebElement> getElements(By by) {
        return await(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    public int countElements(String xPath) throws Exception {
        return Math.toIntExact(executeScript("/nl/juraji/imagemanager/util/io/pinterest/js/count-xpath-elements.js", xPath));
    }

    public <R> R executeScript(String name, Object... args) throws Exception {
        try (InputStream stream = PinterestWebSession.class.getResourceAsStream(name)) {
            String script = IOUtils.toString(stream, "UTF-8");
            //noinspection unchecked
            return (R) driver.executeScript(script, (Object[]) args);
        }
    }

    private void checkDoLogin() throws Exception {
        if (isUnAuthenticated()) {
            cookieJar.setCookies(driver);
            driver.navigate().refresh();

            if (isUnAuthenticated()) {
                navigate(getData("data.urls.pinterest.loginPage"));
                WebElement usernameInput = getElement(by("xpath.loginPage.usernameField"));
                WebElement passwordInput = getElement(by("xpath.loginPage.passwordField"));
                WebElement loginButton = getElement(by("class.loginPage.loginButton"));
                if (usernameInput != null && passwordInput != null) {
                    usernameInput.sendKeys(username);
                    passwordInput.sendKeys(password);
                    loginButton.click();

                    getElement(by("class.mainPage.feed"));
                    cookieJar.storeCookies(driver);
                }
            }
        }
    }

    private boolean isUnAuthenticated() {
        final Cookie authCookie = driver.manage().getCookieNamed("_auth");
        return !(authCookie != null && "1".equals(authCookie.getValue()));
    }

    private <R> R await(ExpectedCondition<R> expectedCondition) {
        return new WebDriverWait(driver, 2, 500)
                .until(expectedCondition);
    }

    public By by(@PropertyKey(resourceBundle = SCRAPER_DATA_BUNDLE_NAME) String key) {
        String type = key.substring(0, key.indexOf("."));
        String value = getData(key);

        switch (type) {
            case "xpath":
                return By.xpath(value);
            case "class":
                return By.className(value);
            default:
                throw new UnsupportedOperationException("Keys of type \"" + type + "\" are not supported");
        }
    }

    public String getData(@PropertyKey(resourceBundle = SCRAPER_DATA_BUNDLE_NAME) String key) {
        return scraperData.getString(key);
    }
}
