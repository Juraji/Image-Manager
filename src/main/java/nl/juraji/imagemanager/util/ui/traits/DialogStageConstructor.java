package nl.juraji.imagemanager.util.ui.traits;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.ui.UIUtils;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface DialogStageConstructor extends SceneConstructor {

    /**
     * Build and show this dialog
     */
    default void show() {
        this.show(Main.getPrimaryStage());
    }

    /**
     * Build and show this dialog with a custom owner
     * @param owner The owner to bind this dialog to
     */
    default void show(Window owner) {
        final Scene scene = createScene();

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().addAll(Main.getPrimaryStage().getIcons());
        stage.setScene(scene);
        stage.setTitle(getWindowTitle());

        Preferences.Scenes.setAndBindMaximizedProperty(stage, getClass().getSimpleName());

        stage.show();
        this.postInitialization();
    }

    /**
     * Close this dialog
     */
    default void close() {
        UIUtils.getStage((Node) this).close();
    }

    /**
     * Implement in subclass to set window title
     * @return A title for this window
     */
    String getWindowTitle();
}
