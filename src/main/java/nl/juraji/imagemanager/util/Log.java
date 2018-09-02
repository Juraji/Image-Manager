package nl.juraji.imagemanager.util;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public class Log {

    public static Logger create(Class owner) {
        return LoggerFactory.getLogger(owner);
    }

    public static Logger create(Object owner) {
        return create(owner.getClass());
    }

    public static void setRootLogDebug(boolean debugMode) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
                org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (debugMode) {
            root.setLevel(Level.DEBUG);
        } else {
            root.setLevel(Level.INFO);
        }
    }
}
