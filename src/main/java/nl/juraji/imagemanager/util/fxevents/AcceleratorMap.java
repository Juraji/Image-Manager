package nl.juraji.imagemanager.util.fxevents;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import nl.juraji.imagemanager.util.streams.BiStream;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 6-9-2018.
 * Image Manager
 */
public final class AcceleratorMap extends HashMap<KeyCombination, AcceleratorMap.NamedRunnable> {

    public static AcceleratorMap emptyMap() {
        return new AcceleratorMap();
    }

    public AcceleratorMap putKey(KeyCode keyCode, Runnable runnable, String name) {
        this.put(new KeyCodeCombination(keyCode), namedRunnable(runnable, name));

        return this;
    }

    public AcceleratorMap putKeyWithControl(KeyCode keyCode, Runnable runnable, String name) {
        this.put(new KeyCodeCombination(keyCode, KeyCombination.CONTROL_DOWN), namedRunnable(runnable, name));
        return this;
    }

    public AcceleratorMap putKeyWithAlt(KeyCode keyCode, Runnable runnable, String name) {
        this.put(new KeyCodeCombination(keyCode, KeyCombination.ALT_DOWN), namedRunnable(runnable, name));
        return this;
    }

    public AcceleratorMap putKeyWithShift(KeyCode keyCode, Runnable runnable, String name) {
        this.put(new KeyCodeCombination(keyCode, KeyCombination.SHIFT_DOWN), namedRunnable(runnable, name));
        return this;
    }

    public AcceleratorMap putKeyWithMeta(KeyCode keyCode, Runnable runnable, String name) {
        this.put(new KeyCodeCombination(keyCode, KeyCombination.META_DOWN), namedRunnable(runnable, name));
        return this;
    }

    public String createTooltipText(ResourceBundle resources) {
        return BiStream.stream(this)
                .mapKey(KeyCombination::toString)
                .mapValue(AcceleratorMap.NamedRunnable::getName)
                .mapValue(resources::getString)
                .sortedByKey(String::compareTo)
                .map((combo, description) -> combo + ": " + description)
                .reduce((l, r) -> l + "\n" + r)
                .orElse(null);
    }

    private NamedRunnable namedRunnable(Runnable runnable, String name) {
        return new NamedRunnable(name) {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    public abstract static class NamedRunnable implements Runnable {
        private final String name;

        private NamedRunnable(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
