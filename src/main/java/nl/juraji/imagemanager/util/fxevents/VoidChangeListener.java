package nl.juraji.imagemanager.util.fxevents;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by Juraji on 6-9-2018.
 * Image Manager
 */
public interface VoidChangeListener extends ChangeListener<Object> {
    @Override
    default void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
        this.changed();
    }

    void changed();
}
