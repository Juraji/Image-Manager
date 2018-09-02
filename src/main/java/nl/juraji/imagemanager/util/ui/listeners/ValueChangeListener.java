package nl.juraji.imagemanager.util.ui.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
@FunctionalInterface
public interface ValueChangeListener<T> extends ChangeListener<T> {
    @Override
    default void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        this.changed(newValue);
    }

    void changed(T newValue);
}
