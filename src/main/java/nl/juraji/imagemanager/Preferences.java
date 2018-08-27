package nl.juraji.imagemanager;

import nl.juraji.imagemanager.util.ExceptionUtils;
import nl.juraji.imagemanager.util.io.FileInputStream;
import nl.juraji.imagemanager.util.io.FileOutputStream;

import java.io.File;
import java.nio.file.NoSuchFileException;
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
        ExceptionUtils.catchAll(() -> {
            File prefFile = new File(PREFERENCES_FILE);
            if (!prefFile.exists()) {
                final boolean created = prefFile.createNewFile();
                if (!created) {
                    throw new NoSuchFileException(prefFile.getAbsolutePath(), "", "Could not create preferences file!");
                }
            }
            this.properties.load(new FileInputStream(prefFile));
        });
    }

    public static Locale getLocale() {
        final String localeKey = read("common.locale", "en");
        return new Locale(localeKey);
    }

    public static void setLocale(Locale locale) {
        persist("common.locale", locale.getLanguage());
    }

    public static int getDirectoryTilesPageSize() {
        return Integer.parseInt(read("editDirectoryController.directoryTilesPageSize", "50"));
    }

    public static void setDirectoryTilesPageSize(int value) {
        persist("editDirectoryController.directoryTilesPageSize", String.valueOf(value));
    }

    public static Double getColumnWidth(String id) {
        final String read = read("editDirectoryController.directoryTable.columnWidth." + id, null);
        return read == null ? null : Double.parseDouble(read);
    }

    public static void setColumnWidth(String id, Number value) {
        persist("editDirectoryController.directoryTable.columnWidth." + id, String.valueOf(value.doubleValue()));
    }

    public static boolean getColumnVisible(String id) {
        return "true".equals(read("editDirectoryController.directoryTable.columnVisible." + id, "true"));
    }

    public static void setColumnVisible(String id, Boolean value) {
        persist("editDirectoryController.directoryTable.columnVisible." + id, String.valueOf(value));
    }

    private static String read(String key, String defaultValue) {
        return new Preferences().properties.getProperty(key, defaultValue);
    }

    private static void persist(String key, String value) {
        final Preferences p = new Preferences();
        p.properties.setProperty(key, value);
        ExceptionUtils.catchAll(() -> p.properties.store(new FileOutputStream(PREFERENCES_FILE), "Image Manager settings"));
    }
}
