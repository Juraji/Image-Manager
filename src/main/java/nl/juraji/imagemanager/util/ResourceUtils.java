package nl.juraji.imagemanager.util;

import nl.juraji.imagemanager.util.ui.UIUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 20-8-2018.
 * Image Manager
 */
public final class ResourceUtils {
    public static final String I18N_RESOURCE_BUNDLE_BASE = "nl.juraji.imagemanager.i18n";

    private ResourceUtils() {
    }

    public static ArrayList<Locale> getAvailableLocales() {
        final ArrayList<Locale> locales = new ArrayList<>();
        final String base = "/" + I18N_RESOURCE_BUNDLE_BASE.replaceAll("\\.", "/") + "_";

        for (String language : Locale.getISOLanguages()) {
            final URL url = UIUtils.class.getResource(base + language + ".properties");
            if (url != null) {
                locales.add(new Locale(language));
            }
        }

        return locales;
    }

    /**
     * Get the application I18n bundle
     *
     * @return A ResourceBundle containing the application i18n bundles
     */
    public static ResourceBundle getLocaleBundle() {
        return ResourceBundle.getBundle(I18N_RESOURCE_BUNDLE_BASE, Preferences.Application.getLocale());
    }
}
