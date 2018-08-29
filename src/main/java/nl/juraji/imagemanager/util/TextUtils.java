package nl.juraji.imagemanager.util;

import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class TextUtils {
    private TextUtils() {
    }

    public static String trim(String value) {
        return value != null ? value.trim() : value;
    }

    public static boolean isEmpty(String... values) {
        return (values == null || values.length == 0) || Arrays.stream(values).anyMatch(s -> s == null || trim(s).length() == 0);
    }

    public static String format(ResourceBundle resources, String key, Object... params) {
        final String string = resources.getString(key);
        return format(string, params);
    }

    public static String format(String pattern, Object... params) {
        if (!isEmpty(pattern) && params != null && params.length != 0) {
            final StringBuilder builder = new StringBuilder(pattern);
            int pi = 0;

            for (Object param : params) {
                final String valueOf = String.valueOf(param);
                final int i = builder.indexOf("{}", pi);

                if (i == -1) {
                    break;
                }

                builder.replace(i, i + 2, valueOf);
                pi = i;
            }

            return builder.toString();
        } else {
            return pattern;
        }
    }

    public static String cutOff(String value, int maxLen) {
        return cutOff(value, maxLen, true);
    }

    public static String cutOff(String value, int maxLen, boolean ellipsis) {
        value = trim(value);
        if (value == null || value.length() < maxLen) {
            return value;
        }

        return value.substring(0, maxLen - 1) + (ellipsis ? "\u2026" : "");
    }

    public static String orDefault(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    public static String getFileSystemSafeName(String value) {
        String result = value.replaceAll("[^0-9a-zA-Z-., ]", "_");
        if (result.length() > 64) result = result.substring(0, 63);
        return result.trim();
    }
}
