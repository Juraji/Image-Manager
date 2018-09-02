package nl.juraji.imagemanager.util;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public final class Log {
    private Log() {
    }

    public static Logger create(Class owner) {
        return LoggerFactory.getLogger(owner);
    }

    public static Logger create(Object owner) {
        return create(owner.getClass());
    }

    public static void enableRootLogDebug() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
    }
}
