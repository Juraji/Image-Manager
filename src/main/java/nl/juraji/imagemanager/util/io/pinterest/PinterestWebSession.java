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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class PinterestWebSession implements AutoCloseable {
    private static final String SCRAPER_BUNDLE = "nl.juraji.imagemanager.util.io.pinterest.scraper-data";

    static {
        final WebDriverManager manager = WebDriverManager.chromedriver();
        manager.targetPath("./");
        manager.setup();
    }

    private final String fullUsername;
    private final String username;
    private final String password;
    private final ResourceBundle scraperData;
    private final CookieJar cookieJar;
    private RemoteWebDriver driver;

    public PinterestWebSession(String username, String password) {
        this.fullUsername = username;
        this.username = username.split("@")[0];
        this.password = password;
        this.scraperData = ResourceBundle.getBundle(SCRAPER_BUNDLE);
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
        navigate(selector("data.urls.pinterest.main") + username);
    }

    /**
     * Navigate to the given url
     */
    public void navigate(String uri) throws Exception {
        if (!driver.getCurrentUrl().equals(uri)) {
            driver.get(uri);

            if (isUnAuthenticated()) {
                cookieJar.loadCookies(driver);

                if (isUnAuthenticated()) {
                    driver.get(selector("data.urls.pinterest.loginPage"));

                    WebElement usernameInput = getElement(by("xpath.loginPage.usernameField"));
                    WebElement passwordInput = getElement(by("xpath.loginPage.passwordField"));
                    WebElement loginButton = getElement(by("class.loginPage.loginButton"));

                    if (usernameInput != null && passwordInput != null) {
                        usernameInput.sendKeys(fullUsername);
                        passwordInput.sendKeys(password);
                        loginButton.click();

                        getElement(by("class.mainPage.feed"));
                        cookieJar.persistCookies(driver, 1, ChronoUnit.DAYS);
                        driver.get(uri);
                    }
                } else {
                    driver.get(uri);
                }
            }
        }
    }

    /**
     * Retrieve a reference to an element on the current page using a By selector
     *
     * @param by The element selector
     * @return The element corresponding to the given selector or
     * an empty element of the selector does not match anything
     */
    public WebElement getElement(By by) {
        return await(ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * Fetches a list of board resources.
     *
     * @throws Exception On script error
     */
    public ArrayList<Map<String, Object>> getPinterestBoardsResource() throws Exception {
        return executeScript(selector("data.scripts.pinterestResources.getBoardsResource"), this.username);
    }

    public Map<String, Object> getPinterestBoardItemsResource(String boardId, String boardUri, String bookmark) throws Exception {
        return executeScript(selector("data.scripts.pinterestResources.getBoardItemsResource"), boardId, boardUri, bookmark);
    }

    /**
     * Execute JavaScript on the current page.
     *
     * @param name The name of the class resource
     * @param args Optional arguments to pass into the script.
     *             These can be retrieved within the script using "arguments[0...]" within the global scope
     * @param <R>  Return type generic
     * @return Any object returned in the script
     * @throws Exception Driver error
     */
    public <R> R executeScript(String name, Object... args) throws Exception {
        try (InputStream stream = PinterestWebSession.class.getResourceAsStream(name)) {
            String script = IOUtils.toString(stream, "UTF-8");
            //noinspection unchecked
            return (R) driver.executeScript(script, (Object[]) args);
        }
    }

    /**
     * Retrieve a By selector for a selector definition within scraper-data.properties
     *
     * @param key A key starting with "class" or "xpath"
     * @return A By instance
     */
    public By by(@PropertyKey(resourceBundle = SCRAPER_BUNDLE) String key) {
        String type = key.substring(0, key.indexOf("."));
        String value = selector(key);

        switch (type) {
            case "xpath":
                return By.xpath(value);
            case "class":
                return By.className(value);
            default:
                throw new UnsupportedOperationException("Keys of type \"" + type + "\" are not supported");
        }
    }

    public String selector(@PropertyKey(resourceBundle = SCRAPER_BUNDLE) String key) {
        return scraperData.getString(key);
    }

    private boolean isUnAuthenticated() {
        final Cookie authCookie = driver.manage().getCookieNamed("_auth");
        return !(authCookie != null && "1".equals(authCookie.getValue()));
    }

    private <R> R await(ExpectedCondition<R> expectedCondition) {
        return new WebDriverWait(driver, 2, 500)
                .until(expectedCondition);
    }
}
