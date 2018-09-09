package nl.juraji.imagemanager.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.ui.scenes.EditDirectoryScene;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.math.FXColors;
import nl.juraji.imagemanager.util.ui.UIUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 9-9-2018.
 * Image Manager
 */
public class DirectoryTile extends Tile {

    private final Directory directory;

    @FXML
    private ImageView directoryImageOutlet;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label subDirectoryCountLabel;
    @FXML
    private Label imageCountLabel;

    public DirectoryTile(Directory directory) {
        this.directory = directory;

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if(directory.isIgnored()){
            directoryImageOutlet.setEffect(FXColors.lightenEffect(0.6));
        }

        directoryLabel.setText(directory.getName());
        subDirectoryCountLabel.setText(TextUtils.format(resources, "DirectoryTile.subDirectoryCountLabel", directory.getSubDirectoryCount()));
        imageCountLabel.setText(TextUtils.format(resources, "DirectoryTile.imageCountLabel", directory.getMetaDataCount()));

        this.setOnMouseClicked(event -> {
            if (UIUtils.isDoublePrimaryClickEvent(event)) {
                Main.getPrimaryScene().pushContent(new EditDirectoryScene(directory));
            }
        });
    }
}
