package nl.juraji.imagemanager.util.fxevents;

import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * Created by Juraji on 9-9-2018.
 * Image Manager
 */
@FunctionalInterface
public interface VoidHandler<T extends Event> extends EventHandler<T> {
    @Override
    default void handle(T event) {
        this.handleVoid();
    }

    void handleVoid();
}
