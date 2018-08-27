package nl.juraji.imagemanager.util.ui;

/**
 * Created by Juraji on 27-8-2018.
 * Image Manager
 */
public class ChoiceProperty<T> {
    private final String displayName;
    private final T value;

    public ChoiceProperty(String displayName, T value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
