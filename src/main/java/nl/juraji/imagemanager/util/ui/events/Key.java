package nl.juraji.imagemanager.util.ui.events;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Created by Juraji on 6-9-2018.
 * Image Manager
 */
public final class Key {

    public static KeyCombination key(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode);
    }

    public static KeyCombination withControl(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.CONTROL_DOWN);
    }

    public static KeyCombination withAlt(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.ALT_DOWN);
    }

    public static KeyCombination withShift(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.SHIFT_DOWN);
    }

    public static KeyCombination withMeta(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCombination.META_DOWN);
    }
}
