package nl.juraji.imagemanager.util.ui.traits;

import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public abstract class BorderPaneScene extends BorderPane implements FXMLConstructor, SceneConstructor, Initializable {
    protected ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }
}
