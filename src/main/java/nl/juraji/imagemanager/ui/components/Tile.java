package nl.juraji.imagemanager.ui.components;

import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import nl.juraji.imagemanager.util.ui.traits.FXMLConstructor;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 9-9-2018.
 * Image Manager
 */
public abstract class Tile extends VBox implements FXMLConstructor, Initializable {

    protected ResourceBundle resources;

    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        this.setOnMouseEntered(this::mouseEnteredHandler);
        this.setOnMouseExited(this::mouseExitedHandler);
    }

    private void mouseEnteredHandler(MouseEvent event) {
        event.consume();
        this.setStyle("-fx-background-color: lightgray;");
    }

    private void mouseExitedHandler(MouseEvent event) {
        event.consume();
        this.setStyle(null);
    }
}
