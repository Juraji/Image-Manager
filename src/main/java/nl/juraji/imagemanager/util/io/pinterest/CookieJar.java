package nl.juraji.imagemanager.util.io.pinterest;

import nl.juraji.imagemanager.util.io.FileInputStream;
import nl.juraji.imagemanager.util.io.FileOutputStream;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class CookieJar {
    private final String storePath;

    public CookieJar(String storePath) {
        this.storePath = storePath;
    }

    public void storeCookies(RemoteWebDriver driver) throws IOException {
        final Properties cookieStore = new Properties();
        final Set<Cookie> cookies = driver.manage().getCookies();

        for (Cookie cookie : cookies) {
            cookieStore.setProperty(cookie.getName(), encodeCookie(cookie));
        }

        cookieStore.store(new FileOutputStream(storePath), "Image Manager web cookie jar");
    }

    public void loadCookies(RemoteWebDriver driver) throws IOException, ClassNotFoundException {
        final Properties cookieStore = new Properties();
        final File file = new File(storePath);

        if (file.exists()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                cookieStore.load(stream);
            }

            for (Object value : cookieStore.values()) {
                final Cookie cookie = decodeCookie((String) value);
                driver.manage().addCookie(cookie);
            }
        }
    }

    public void deleteCookies() throws IOException {
        final File file = new File(storePath);
        Files.deleteIfExists(file.toPath());
    }

    private Cookie decodeCookie(String encodedCookie) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(encodedCookie);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (Cookie) ois.readObject();
            }
        }
    }

    private String encodeCookie(Cookie cookie) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(cookie);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        }
    }
}
