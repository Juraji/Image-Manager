package nl.juraji.imagemanager.util;

import javafx.stage.Stage;
import nl.juraji.imagemanager.util.fxevents.ValueChangeListener;
import nl.juraji.imagemanager.util.io.FileInputStream;
import nl.juraji.imagemanager.util.io.FileOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by Juraji on 20-8-2018.
 * Image Manager
 */
public final class Preferences {
    private static final String PREFERENCES_FILE = "./preferences.properties";

    private final Properties properties;

    private Preferences() {
        this.properties = new Properties();

        try {
            File prefFile = new File(PREFERENCES_FILE);
            if (!prefFile.exists()) {
                final boolean created = prefFile.createNewFile();
                if (!created) {
                    throw new NoSuchFileException(prefFile.getAbsolutePath(), "", "Could not create preferences file!");
                }
            }

            try (FileInputStream stream = new FileInputStream(prefFile)) {
                this.properties.load(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String read(String key, String defaultValue) {
        return new Preferences().properties.getProperty(key, defaultValue);
    }

    private static void persist(String key, String value) {
        final Preferences p = new Preferences();
        p.properties.setProperty(key, value);
        ExceptionUtils.catchAll(() -> p.properties.store(new FileOutputStream(PREFERENCES_FILE), "Image Manager settings"));
    }

    public static class Application {
        public static Locale getLocale() {
            final String localeKey = read("Application.locale", "en");
            return new Locale(localeKey);
        }

        public static void setLocale(Locale locale) {
            persist("Application.locale", locale.getLanguage());
        }

        public static boolean isDebugMode() {
            return "true".equals(read("Application.debugMode", "false"));
        }

        public static void setDebugMode(boolean enabled) {
            persist("Application.debugMode", String.valueOf(enabled));
        }
    }

    public static class Pinterest {
        public static String[] getLogin() {
            final String v = read("Service.pinterest.login", null);

            String[] login = null;
            if (v != null) {
                final String decode = new String(Base64.getDecoder().decode(v));
                login = decode.split(":");
            }

            return login;
        }

        public static void setLogin(String username, String password) {
            final String s = username + ":" + password;
            final byte[] encode = Base64.getEncoder().encode(s.getBytes());
            persist("Service.pinterest.login", new String(encode));
        }

        public static File getTargetDirectory() {
            return new File(read("Service.pinterest.targetDirectory", System.getProperty("user.home")));
        }

        public static void setTargetDirectory(File target) {
            persist("Service.pinterest.targetDirectory", target.getAbsolutePath());
        }
    }

    public static class Scenes {
        public static void setAndBindMaximizedProperty(Stage stage, String stageName) {
            final boolean wasMaximized = "true".equals(read("Scenes.wasMaximized." + stageName, "false"));
            stage.setMaximized(wasMaximized);

            stage.maximizedProperty().addListener((ValueChangeListener<Boolean>) newValue ->
                    persist("Scenes.wasMaximized." + stageName, String.valueOf(newValue)));
        }

        public static class EditDirectory {
            public static int getPageSize() {
                return Integer.parseInt(read("Scenes.EditDirectory.directoryTilesPageSize", "50"));
            }

            public static void setPageSize(int value) {
                persist("Scenes.EditDirectory.directoryTilesPageSize", String.valueOf(value));
            }

            public static Double getColumnWidth(String id) {
                final String read = read("Scenes.EditDirectory.directoryTable.columnWidth." + id, null);
                return read == null ? null : Double.parseDouble(read);
            }

            public static void setColumnWidth(String id, Number value) {
                persist("Scenes.EditDirectory.directoryTable.columnWidth." + id, String.valueOf(value.doubleValue()));
            }

            public static boolean getColumnVisible(String id) {
                return "true".equals(read("Scenes.EditDirectory.directoryTable.columnVisible." + id, "true"));
            }

            public static void setColumnVisible(String id, Boolean value) {
                persist("Scenes.EditDirectory.directoryTable.columnVisible." + id, String.valueOf(value));
            }
        }
    }
}
