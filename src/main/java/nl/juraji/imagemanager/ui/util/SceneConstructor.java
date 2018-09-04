package nl.juraji.imagemanager.ui.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface SceneConstructor {

    default Node getContentNode(){
        return (Node) this;
    }

    default Scene createScene() {
        return new Scene((Parent) this);
    }
}
