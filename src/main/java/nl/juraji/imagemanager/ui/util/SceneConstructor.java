package nl.juraji.imagemanager.ui.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface SceneConstructor {

    default Node getContentNode() {
        return (Node) this;
    }

    default Scene createScene() {
        final Scene scene = new Scene((Parent) this);

        scene.getAccelerators().putAll(getAccelerators());
        getMnemonics().forEach(scene::addMnemonic);

        return scene;
    }

    default List<Mnemonic> getMnemonics() {
        return Collections.emptyList();
    }

    default Map<KeyCombination, Runnable> getAccelerators() {
        return Collections.emptyMap();
    }
}
