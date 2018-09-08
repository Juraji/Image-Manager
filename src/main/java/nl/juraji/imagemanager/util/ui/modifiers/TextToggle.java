package nl.juraji.imagemanager.util.ui.modifiers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Juraji on 8-9-2018.
 * Image Manager
 */
public class TextToggle {
    private final List<String> values;
    private final StringProperty currentValue;
    private int nextIndex = 0;

    public TextToggle(String... values) {
        this.values = new ArrayList<>();
        this.values.addAll(Arrays.asList(values));
        this.currentValue = new SimpleStringProperty(this.values.get(0));
    }

    public String getCurrentValue() {
        return currentValue.get();
    }

    public StringProperty currentValueProperty() {
        return currentValue;
    }

    public void setNext() {
        ++nextIndex;

        if (nextIndex >= values.size()) {
            nextIndex = 0;
        }

        this.currentValue.setValue(this.values.get(nextIndex));
    }
}
