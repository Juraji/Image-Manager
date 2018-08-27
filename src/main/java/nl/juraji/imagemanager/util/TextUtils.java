package nl.juraji.imagemanager.util;

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

    public static boolean isEmpty(Object[] value) {
        return value == null || value.length == 0;
    }

    public static boolean isEmpty(String value) {
        return value == null || trim(value).length() == 0;
    }

    public static String format(ResourceBundle resources, String key, Object... params) {
        final String string = resources.getString(key);
        return format(string, params);
    }

    public static String format(String pattern, Object... params) {
        if (!TextUtils.isEmpty(params)) {
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
}
