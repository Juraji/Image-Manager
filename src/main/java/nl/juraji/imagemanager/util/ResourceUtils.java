package nl.juraji.imagemanager.util;

import nl.juraji.imagemanager.util.ui.UIUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Juraji on 20-8-2018.
 * Image Manager
 */
public final class ResourceUtils {
    public static final String I18N_RESOURCE_BUNDLE_BASE = "nl.juraji.imagemanager.i18n.bundle";

    private ResourceUtils() {
    }

    public static ArrayList<DisplayLocale> getAvailableLocales() {
        final ArrayList<DisplayLocale> locales = new ArrayList<>();
        final String base = "/" + I18N_RESOURCE_BUNDLE_BASE.replaceAll("\\.", "/") + "_";

        for (String language : Locale.getISOLanguages()) {
            final URL url = UIUtils.class.getResource(base + language + ".properties");
            if (url != null) {
                final DisplayLocale locale = new DisplayLocale(new Locale(language));
                locales.add(locale);
            }
        }

        return locales;
    }

    public static class DisplayLocale {
        private final Locale locale;

        private DisplayLocale(Locale locale){
            this.locale = locale;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return this.locale.getDisplayLanguage();
        }
    }
}
