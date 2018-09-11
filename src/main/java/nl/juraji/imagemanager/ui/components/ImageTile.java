package nl.juraji.imagemanager.ui.components;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ChoiceProperty;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.dialogs.ViewImageDialog;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.fxevents.VoidHandler;
import nl.juraji.imagemanager.util.ui.ImageUtils;
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
public class ImageTile extends Tile<ImageMetaData> {
    private static final double PREFERRED_IMG_DIM = 190;

    private final List<ImageMetaData> availableImageMetaData;

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

    public ImageTile(TilePane parent, ImageMetaData imageMetaData) {
        this(parent, imageMetaData, null);
    }

    public ImageTile(TilePane parent, ImageMetaData imageMetaData, List<ImageMetaData> available) {
        super(parent, imageMetaData);

        this.constructFXML();
        this.availableImageMetaData = available;

        directoryLabel.setText(TextUtils.cutOff(imageMetaData.getDirectoryName(), 30));
        fileNameLabel.setText(TextUtils.cutOff(imageMetaData.getFile().getName(), 30));
        fileSizeLabel.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));

        final String formattedDateAdded = UIUtils.formatDateTime(imageMetaData.getDateAdded(), FormatStyle.SHORT);
        dateAddedLabel.setText(formattedDateAdded);

        imageDimensionsLabel.setText(TextUtils.format(resources,
                "common.imageDimensions.label",
                imageMetaData.getImageWidth(), imageMetaData.getImageHeight()));

        Platform.runLater(() -> {
            final Image image = ImageUtils.safeLoadImage(imageMetaData.getFile(), PREFERRED_IMG_DIM, PREFERRED_IMG_DIM);
            imageViewStackPane.getChildren().remove(0);
            if (image != null) {
                imageContainer.setImage(image);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        this.setOnMouseClicked(e -> {
            if (UIUtils.isDoublePrimaryClickEvent(e)) {
                contextMenuOpenFileAction();
            }
        });
    }

    @FXML
    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        ContextMenu menu = new ContextMenu();

        final MenuItem openFileAction = new MenuItem();
        openFileAction.setText(resources.getString("ImageTile.contextMenuOpenFileAction.label"));
        openFileAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuOpenFileAction);
        menu.getItems().add(openFileAction);

        final MenuItem moveToAction = new MenuItem();
        moveToAction.setText(resources.getString("ImageTile.contextMenuMoveToAction.label"));
        moveToAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuMoveToAction);
        menu.getItems().add(moveToAction);

        if (tileData instanceof PinMetaData) {
            final MenuItem openOnPinterestAction = new MenuItem();
            openOnPinterestAction.setText(resources.getString("ImageTile.contextMenuOpenOnPinterestAction.label"));
            openOnPinterestAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuOpenOnPinterestAction);
            menu.getItems().add(openOnPinterestAction);
        }

        final MenuItem deleteFileAction = new MenuItem();
        deleteFileAction.setText(resources.getString("ImageTile.contextMenuDeleteFileAction.label"));
        deleteFileAction.setOnAction((VoidHandler<ActionEvent>) this::contextMenuDeleteFileAction);
        menu.getItems().add(deleteFileAction);

        menu.setX(contextMenuEvent.getScreenX());
        menu.setY(contextMenuEvent.getScreenY());
        menu.setAutoHide(true);
        menu.show(UIUtils.getStage(contextMenuEvent));
    }

    private void contextMenuOpenFileAction() {
        final ViewImageDialog dialog = new ViewImageDialog(this.tileData);
        dialog.setAvailableImageMetaData(availableImageMetaData);
        dialog.show();
    }

    private void contextMenuMoveToAction() {
        final Dao dao = new Dao();
        final String currentDirectoryName = this.tileData.getDirectoryName();

        final List<ChoiceProperty<Directory>> list = dao.getAllDirectories().stream()
                .filter(d -> !d.getName().equals(currentDirectoryName))
                .map(d -> new ChoiceProperty<>(d.getName(), d))
                .collect(Collectors.toList());

        final ChoiceDialog<ChoiceProperty<Directory>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("ImageTile.contextMenuMoveToAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources.getString("ImageTile.contextMenuMoveToAction.dialog.moveButton.label"));
        dialog.showAndWait().ifPresent(choice -> {
            try {
                final File source = tileData.getFile();
                final File target = new File(choice.getValue().getTargetLocation(), source.getName());

                Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.tileData.setDirectory(choice.getValue());
                this.tileData.setDateAdded(LocalDateTime.now());
                this.tileData.setFile(target);

                dao.save(this.tileData);

                ToastBuilder.create()
                        .withMessage(TextUtils.format(resources, "ImageTile.contextMenuMoveToAction.toast",
                                source.getName(), currentDirectoryName, choice.getDisplayName()))
                        .show();

                this.removeSelfFromParent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void contextMenuOpenOnPinterestAction() {
        UIUtils.desktopOpen(((PinMetaData) tileData).getPinterestUri());
    }

    private void contextMenuDeleteFileAction() {
        AlertBuilder.createWarning()
                .withTitle(resources.getString("ImageTile.contextMenuDeleteFileAction.warning.title"))
                .withContext(resources.getString("ImageTile.contextMenuDeleteFileAction.warning.context"), tileData.getFile().getName())
                .show(() -> {
                    try {
                        Files.deleteIfExists(tileData.getFile().toPath());

                        final Dao dao = new Dao();
                        dao.delete(tileData);

                        ToastBuilder.create()
                                .withMessage(resources.getString("ImageTile.contextMenuDeleteFileAction.toast"), tileData.getFile().getName())
                                .show();

                        this.removeSelfFromParent();
                        Main.getPrimaryScene().updateStatusBar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
