package nl.juraji.imagemanager.ui.util;

import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.util.ui.UIUtils;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface DialogStageConstructor extends SceneConstructor {

    default void show() {
        this.show(Main.getPrimaryStage());
    }

    default void show(Window owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().addAll(Main.getPrimaryStage().getIcons());
        stage.setScene(createScene());
        stage.show();
    }

    default void close() {
        UIUtils.getStage((Node) this).close();
    }
}