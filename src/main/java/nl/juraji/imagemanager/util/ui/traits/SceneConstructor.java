package nl.juraji.imagemanager.util.ui.traits;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Mnemonic;
import nl.juraji.imagemanager.util.fxevents.AcceleratorMap;

import java.util.Collections;
import java.util.List;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface SceneConstructor {

    /**
     * @return This as {@link Node}
     */
    default Node getContentNode() {
        return (Node) this;
    }

    /**
     * @return This wrapped in a {@link Scene}
     */
    default Scene createScene() {
        final Scene scene = new Scene((Parent) this);

        scene.getAccelerators().putAll(getAccelerators());
        getMnemonics().forEach(scene::addMnemonic);

        return scene;
    }

    /**
     * Implement in subclass to add mnemonics to {@link #createScene}
     *
     * @return A list of mnemonics
     */
    default List<Mnemonic> getMnemonics() {
        return Collections.emptyList();
    }

    /**
     * Implement in subclass to add accelerators to {@link #createScene}
     *
     * @return A map of accelerator definitions
     */
    default AcceleratorMap getAccelerators() {
        return AcceleratorMap.emptyMap();
    }

    /**
     * Implement in subclass to do work after initialization
     */
    default void postInitialization() {
        // Do nothing by default
    }

    /**
     * Implement in subclass to do work after being retrieved from scene history
     */
    default void postReloadedInView() {
        // Do nothing by default
    }

    /**
     * Implement in subclass to do work after being pushed to history (or unloaded from view)
     */
    default void preUnloadedFromView() {
    }
}
