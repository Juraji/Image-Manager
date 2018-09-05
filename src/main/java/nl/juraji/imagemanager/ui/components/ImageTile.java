package nl.juraji.imagemanager.ui.components;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ChoiceProperty;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.dialogs.EditImageDialog;
import nl.juraji.imagemanager.ui.util.FXMLConstructor;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.UIUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class ImageTile extends VBox implements FXMLConstructor, Initializable {
    private static final double PREFERRED_IMG_DIM = 190;

    private ResourceBundle resources;
    private ImageMetaData imageMetaData;

    @FXML
    private StackPane imageViewStackPane;
    @FXML
    private ImageView imageContainer;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label dateAddedLabel;
    @FXML
    private Label imageDimensionsLabel;
    @FXML
    private Label fileSizeLabel;

    public ImageTile() {
        this.constructFXML();
    }

    public ImageTile(ImageMetaData imageMetaData) {
        this();
        this.setImageMetaData(imageMetaData);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        this.setOnMouseClicked(e -> {
            if(UIUtils.isDoublePrimaryClickEvent(e)){
                contextMenuOpenFileAction(new ActionEvent(e.getSource(), e.getTarget()));
            }
        });
    }

    public ImageMetaData getImageMetaData() {
        return imageMetaData;
    }

    public void setImageMetaData(ImageMetaData imageMetaData) {
        this.imageMetaData = imageMetaData;

        directoryLabel.setText(TextUtils.cutOff(imageMetaData.getDirectoryName(), 30));
        fileNameLabel.setText(TextUtils.cutOff(imageMetaData.getFile().getName(), 30));
        fileSizeLabel.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));

        final String formattedDateAdded = UIUtils.formatDateTime(imageMetaData.getDateAdded(), FormatStyle.SHORT);
        dateAddedLabel.setText(formattedDateAdded);

        if (imageMetaData.getImageHash() == null) {
            imageDimensionsLabel.setText(null);
        } else {
            imageDimensionsLabel.setText(TextUtils.format(resources,
                    "common.imageDimensions.label",
                    imageMetaData.getImageWidth(), imageMetaData.getImageHeight()));
        }

        Platform.runLater(() -> {
            final Image image = UIUtils.safeLoadImage(imageMetaData.getFile(), PREFERRED_IMG_DIM, PREFERRED_IMG_DIM);
            imageViewStackPane.getChildren().remove(0);
            if (image != null) {
                imageContainer.setImage(image);
            }
        });
    }

    @FXML
    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        ContextMenu menu = new ContextMenu();

        final MenuItem openFileAction = new MenuItem();
        openFileAction.setText(resources.getString("ImageTile.contextMenuOpenFileAction.label"));
        openFileAction.setOnAction(this::contextMenuOpenFileAction);
        menu.getItems().add(openFileAction);

        final MenuItem moveToAction = new MenuItem();
        moveToAction.setText(resources.getString("ImageTile.contextMenuMoveToAction.label"));
        moveToAction.setOnAction(this::contextMenuMoveToAction);
        menu.getItems().add(moveToAction);

        if (imageMetaData instanceof PinMetaData) {
            final MenuItem openOnPinterestAction = new MenuItem();
            openOnPinterestAction.setText(resources.getString("ImageTile.contextMenuOpenOnPinterestAction.label"));
            openOnPinterestAction.setOnAction(this::contextMenuOpenOnPinterestAction);
            menu.getItems().add(openOnPinterestAction);
        }

        final MenuItem deleteFileAction = new MenuItem();
        deleteFileAction.setText(resources.getString("ImageTile.contextMenuDeleteFileAction.label"));
        deleteFileAction.setOnAction(this::contextMenuDeleteFileAction);
        menu.getItems().add(deleteFileAction);

        menu.setX(contextMenuEvent.getScreenX());
        menu.setY(contextMenuEvent.getScreenY());
        menu.setAutoHide(true);
        menu.show(UIUtils.getStage(contextMenuEvent));
    }

    private void contextMenuOpenFileAction(ActionEvent actionEvent) {
        actionEvent.consume();
        new EditImageDialog(this.imageMetaData).show();
    }

    private void contextMenuMoveToAction(ActionEvent actionEvent) {
        actionEvent.consume();
        final Dao dao = new Dao();
        final String currentDirectoryName = this.imageMetaData.getDirectoryName();

        final List<ChoiceProperty<Directory>> list = dao.get(Directory.class).stream()
                .filter(d -> !d.getName().equals(currentDirectoryName))
                .map(d -> new ChoiceProperty<>(d.getName(), d))
                .collect(Collectors.toList());

        final ChoiceDialog<ChoiceProperty<Directory>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("ImageTile.contextMenuMoveToAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources.getString("ImageTile.contextMenuMoveToAction.dialog.moveButton.label"));
        dialog.showAndWait().ifPresent(choice -> {
            try {
                final File source = imageMetaData.getFile();
                final File target = new File(choice.getValue().getTargetLocation(), source.getName());

                Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.imageMetaData.setDirectory(choice.getValue());
                this.imageMetaData.setDateAdded(LocalDateTime.now());
                this.imageMetaData.setFile(target);

                this.directoryLabel.setText(TextUtils.format(resources, "ImageTile.directoryLabel.moved", choice.getDisplayName()));
                dao.save(this.imageMetaData);

                ToastBuilder.create()
                        .withMessage(TextUtils.format(resources, "ImageTile.contextMenuMoveToAction.toast",
                                source.getName(), currentDirectoryName, choice.getDisplayName()))
                        .show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void contextMenuOpenOnPinterestAction(ActionEvent actionEvent) {
        actionEvent.consume();
        UIUtils.desktopOpen(((PinMetaData) imageMetaData).getPinterestUri());
    }

    private void contextMenuDeleteFileAction(ActionEvent actionEvent) {
        actionEvent.consume();
        AlertBuilder.createWarning()
                .withTitle(resources.getString("ImageTile.contextMenuDeleteFileAction.warning.title"))
                .withContext(resources.getString("ImageTile.contextMenuDeleteFileAction.warning.context"), imageMetaData.getFile().getName())
                .show(() -> {
                    try {
                        Files.deleteIfExists(imageMetaData.getFile().toPath());

                        final Dao dao = new Dao();
                        dao.delete(imageMetaData);

                        ToastBuilder.create()
                                .withMessage(resources.getString("ImageTile.contextMenuDeleteFileAction.toast"), imageMetaData.getFile().getName())
                                .show();

                        this.imageContainer.setImage(null);
                        this.directoryLabel.setText(TextUtils.format(resources, "ImageTile.directoryLabel.deleted", this.directoryLabel.getText()));

                        Main.getPrimaryScene().updateStatusBar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
