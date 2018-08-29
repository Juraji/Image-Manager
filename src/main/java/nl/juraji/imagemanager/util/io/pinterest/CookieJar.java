package nl.juraji.imagemanager.util.io.pinterest;

import nl.juraji.imagemanager.util.io.FileInputStream;
import nl.juraji.imagemanager.util.io.FileOutputStream;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class CookieJar {
    private static final String FILE_SUFFIX = ".cookies.properties";
    private static final int JAR_MAX_AGE = 4;

    private final String jarName;
    private final File storageFile;

    public CookieJar(String jarName) {
        this.storageFile = new File("./" + jarName + FILE_SUFFIX);
        this.jarName = jarName;
    }

    public void storeCookies(RemoteWebDriver driver) throws IOException {
        final Properties cookieStore = new Properties();
        final Set<Cookie> cookies = driver.manage().getCookies();

        if (!storageFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            storageFile.createNewFile();
        }

        for (Cookie cookie : cookies) {
            cookieStore.setProperty(cookie.getName(), encodeCookie(cookie));
        }

        cookieStore.store(new FileOutputStream(storageFile), "Image Manager cookie jar: " + jarName);
    }

    public void setCookies(RemoteWebDriver driver) throws IOException, ClassNotFoundException {
        final Properties cookieStore = new Properties();

        if (checkJarAge()) {
            deleteCookies();
            return;
        }

        if (storageFile.exists()) {
            try (FileInputStream stream = new FileInputStream(storageFile)) {
                cookieStore.load(stream);
            }

            for (Object value : cookieStore.values()) {
                final Cookie cookie = decodeCookie((String) value);
                driver.manage().addCookie(cookie);
            }
        }
    }

    public void deleteCookies() throws IOException {
        Files.deleteIfExists(storageFile.toPath());
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

    private boolean checkJarAge() {
        return Instant.ofEpochMilli(storageFile.lastModified())
                .plus(JAR_MAX_AGE, ChronoUnit.HOURS)
                .isBefore(Instant.now());
    }
}
