package nl.juraji.imagemanager.fxml.controls;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.components.ChoiceProperty;
import nl.juraji.imagemanager.components.builders.AlertBuilder;
import nl.juraji.imagemanager.components.builders.ToastBuilder;
import nl.juraji.imagemanager.fxml.dialogs.EditImageController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.FXML;
import nl.juraji.imagemanager.util.ui.InitializableWithData;
import nl.juraji.imagemanager.util.ui.UIUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ImageTileController implements InitializableWithData<ImageMetaData> {

    private static final double PREFERRED_IMG_DIM = 190;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

    private ImageMetaData imageMetaData;
    private ResourceBundle resources;

    public StackPane imageViewStackPane;
    public ImageView imageContainer;
    public Label directoryLabel;
    public Label fileNameLabel;
    public Label dateAddedLabel;
    public Label imageDimensionsLabel;
    public Label fileSizeLabel;

    public static Parent createDefaultTile(ImageMetaData imageMetaData) {
        final Parent view = FXML.createView(ImageTileController.class, imageMetaData);

        view.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (UIUtils.isDoublePrimaryClickEvent(event)) {
                EditImageController.showAsDialog(imageMetaData);
            }
        });

        return view;
    }

    @Override
    public void initializeWithData(URL location, ResourceBundle resources, ImageMetaData data) {
        this.imageMetaData = data;
        this.resources = resources;

        directoryLabel.setText(TextUtils.cutOff(imageMetaData.getDirectoryName(), 30));
        fileNameLabel.setText(TextUtils.cutOff(imageMetaData.getFile().getName(), 30));
        dateAddedLabel.setText(imageMetaData.getDateAdded().format(DT_FMT));
        fileSizeLabel.setText(FileUtils.bytesInHumanReadable(imageMetaData.getFileSize()));

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

    public void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        ContextMenu menu = new ContextMenu();

        final MenuItem openFileAction = new MenuItem();
        openFileAction.setText(resources.getString("editDirectoryImageTileController.contextMenuOpenFileAction.label"));
        openFileAction.setOnAction(this::contextMenuOpenFileAction);
        menu.getItems().add(openFileAction);

        final MenuItem moveToAction = new MenuItem();
        moveToAction.setText(resources.getString("editDirectoryImageTileController.contextMenuMoveToAction.label"));
        moveToAction.setOnAction(this::contextMenuMoveToAction);
        menu.getItems().add(moveToAction);

        if (imageMetaData instanceof PinMetaData) {
            final MenuItem openOnPinterestAction = new MenuItem();
            openOnPinterestAction.setText(resources.getString("editDirectoryImageTileController.contextMenuOpenOnPinterestAction.label"));
            openOnPinterestAction.setOnAction(this::contextMenuOpenOnPinterestAction);
            menu.getItems().add(openOnPinterestAction);
        }

        final MenuItem deleteFileAction = new MenuItem();
        deleteFileAction.setText(resources.getString("editDirectoryImageTileController.contextMenuDeleteFileAction.label"));
        deleteFileAction.setOnAction(this::contextMenuDeleteFileAction);
        menu.getItems().add(deleteFileAction);

        menu.setX(contextMenuEvent.getScreenX());
        menu.setY(contextMenuEvent.getScreenY());
        menu.setAutoHide(true);
        menu.show(((BorderPane) contextMenuEvent.getSource()).getScene().getWindow());
    }

    public void contextMenuOpenFileAction(ActionEvent actionEvent) {
        EditImageController.showAsDialog(imageMetaData);
    }

    private void contextMenuMoveToAction(ActionEvent actionEvent) {
        final Dao dao = new Dao();
        final String currentDirectoryName = this.imageMetaData.getDirectoryName();

        final List<ChoiceProperty<Directory>> list = dao.get(Directory.class).stream()
                .filter(d -> !d.getName().equals(currentDirectoryName))
                .map(d -> new ChoiceProperty<>(d.getName(), d))
                .collect(Collectors.toList());

        final ChoiceDialog<ChoiceProperty<Directory>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("editDirectoryImageTileController.contextMenuMoveToAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources.getString("editDirectoryImageTileController.contextMenuMoveToAction.dialog.moveButton.label"));
        dialog.showAndWait().ifPresent(choice -> {
            try {
                final File source = imageMetaData.getFile();
                final File target = new File(choice.getValue().getTargetLocation(), source.getName());

                Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.imageMetaData.setDirectory(choice.getValue());
                this.imageMetaData.setDateAdded(LocalDateTime.now());
                this.imageMetaData.setFile(target);

                this.directoryLabel.setText(TextUtils.format(resources, "editDirectoryImageTileController.directoryLabel.moved", choice.getDisplayName()));
                dao.save(this.imageMetaData);

                ToastBuilder.create()
                        .withMessage(TextUtils.format(resources, "editDirectoryImageTileController.contextMenuMoveToAction.toast",
                                source.getName(), currentDirectoryName, choice.getDisplayName()))
                        .show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void onContainerMouseEntered(MouseEvent mouseEvent) {
        AnchorPane container = (AnchorPane) mouseEvent.getSource();
        container.setStyle("-fx-background-color: lightblue;");
    }

    public void onContainerMouseExited(MouseEvent mouseEvent) {
        AnchorPane container = (AnchorPane) mouseEvent.getSource();
        container.setStyle(null);
    }

    private void contextMenuOpenOnPinterestAction(ActionEvent actionEvent) {
        UIUtils.desktopOpen(((PinMetaData) imageMetaData).getPinterestUri());
    }

    private void contextMenuDeleteFileAction(ActionEvent actionEvent) {
        AlertBuilder.createWarning()
                .withTitle(resources.getString("editDirectoryImageTileController.contextMenuDeleteFileAction.warning.title"))
                .withContext(resources.getString("editDirectoryImageTileController.contextMenuDeleteFileAction.warning.context"), imageMetaData.getFile().getName())
                .show(() -> {
                    try {
                        Files.deleteIfExists(imageMetaData.getFile().toPath());

                        final Dao dao = new Dao();
                        dao.delete(imageMetaData);

                        ToastBuilder.create()
                                .withMessage(resources.getString("editDirectoryImageTileController.contextMenuDeleteFileAction.toast"), imageMetaData.getFile().getName())
                                .show();

                        this.imageContainer.setImage(null);
                        this.directoryLabel.setText(TextUtils.format(resources, "editDirectoryImageTileController.directoryLabel.deleted", this.directoryLabel.getText()));

                        Main.getPrimaryController().updateStatusBar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
