package nl.juraji.imagemanager.ui.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.scenes.DirectoryScene;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.fxevents.VoidHandler;
import nl.juraji.imagemanager.util.math.FXColors;
import nl.juraji.imagemanager.util.ui.UIUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 9-9-2018.
 * Image Manager
 */
public class DirectoryTile extends Tile<Directory> {

    @FXML
    private ImageView directoryImageOutlet;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label subDirectoryCountLabel;
    @FXML
    private Label imageCountLabel;

    public DirectoryTile(TilePane parent, Directory directory) {
        super(parent, directory);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if (tileData.isIgnored()) {
            directoryImageOutlet.setEffect(FXColors.lightenEffect(0.6));
            directoryLabel.setText(TextUtils.format(resources, "DirectoryTile.directoryLabel.ignored", tileData.getName()));
        } else {
            directoryLabel.setText(tileData.getName());
        }

        subDirectoryCountLabel.setText(TextUtils.format(resources, "DirectoryTile.subDirectoryCountLabel", tileData.getSubDirectoryCount()));
        imageCountLabel.setText(TextUtils.format(resources, "DirectoryTile.imageCountLabel", tileData.getMetaDataCount()));

        this.setOnMouseClicked(event -> {
            if (UIUtils.isDoublePrimaryClickEvent(event)) {
                this.contextMenuOpenDirectoryAction();
            }
        });
    }

    @FXML
    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        ContextMenu menu = new ContextMenu();

        final MenuItem openFileAction = new MenuItem();
        openFileAction.setText(resources.getString("DirectoryTile.contextMenuOpenDirectoryAction.label"));
        openFileAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuOpenDirectoryAction);
        menu.getItems().add(openFileAction);

        final MenuItem moveToAction = new MenuItem();
        moveToAction.setText(resources.getString("DirectoryTile.contextOpenInExplorer.label"));
        moveToAction.setOnAction((VoidHandler<ActionEvent>) this::contextOpenInExplorerAction);
        menu.getItems().add(moveToAction);

        if (tileData instanceof PinterestBoard) {
            final MenuItem openOnPinterestAction = new MenuItem();
            openOnPinterestAction.setText(resources.getString("DirectoryTile.contextMenuOpenOnPinterestAction.label"));
            openOnPinterestAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuOpenOnPinterestAction);
            menu.getItems().add(openOnPinterestAction);
        }

        final MenuItem deleteFileAction = new MenuItem();
        deleteFileAction.setText(resources.getString("DirectoryTile.contextMenuDeleteFileAction.label"));
        deleteFileAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuDeleteFileAction);
        menu.getItems().add(deleteFileAction);

        menu.setX(contextMenuEvent.getScreenX());
        menu.setY(contextMenuEvent.getScreenY());
        menu.setAutoHide(true);
        menu.show(UIUtils.getStage(contextMenuEvent));
    }

    private void contextMenuOpenDirectoryAction() {
        Main.getPrimaryScene().pushContent(new DirectoryScene(tileData));
    }

    private void contextOpenInExplorerAction() {
        UIUtils.desktopOpen(tileData.getTargetLocation());
    }

    private void contextMenuOpenOnPinterestAction() {
        UIUtils.desktopOpen(((PinterestBoard) tileData).getBoardUrl());
    }

    private void contextMenuDeleteFileAction() {
        AlertBuilder.createWarning()
                .withTitle(resources.getString("DirectoryTile.contextMenuDeleteFileAction.warning.title"))
                .withContext(resources.getString("DirectoryTile.contextMenuDeleteFileAction.warning.context"), tileData.getName())
                .show(() -> {
                    try {
                        FileUtils.deleteIfExists(tileData.getTargetLocation());

                        final Dao dao = new Dao();
                        dao.delete(tileData);

                        ToastBuilder.create()
                                .withMessage(resources.getString("DirectoryTile.contextMenuDeleteFileAction.toast"), tileData.getName())
                                .show();

                        this.removeSelfFromParent();
                        Main.getPrimaryScene().updateStatusBar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
