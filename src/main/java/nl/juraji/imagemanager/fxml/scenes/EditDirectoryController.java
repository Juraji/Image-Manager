package nl.juraji.imagemanager.fxml.scenes;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.Preferences;
import nl.juraji.imagemanager.dialogs.AlertBuilder;
import nl.juraji.imagemanager.dialogs.ToastBuilder;
import nl.juraji.imagemanager.fxml.controls.ImageTileController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.SyncDeletedFilesTask;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.InitializableWithData;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public class EditDirectoryController implements InitializableWithData<Directory> {

    private final Dao dao = new Dao();

    private Directory directory;
    private ResourceBundle resources;
    private EditableFieldContainer editableFieldContainer;

    public Button saveButton;
    public MenuItem clearImageMetaDataAction;

    public Label directoryLabel;
    public Label imageCountLabel;

    public Pagination pagination;
    public ChoiceBox<Integer> pageSizeChoiceBox;
    public ScrollPane imageOutletScrollPane;
    public TilePane imageOutlet;

    public GridPane modelFieldGrid;

    @Override
    public void initializeWithData(URL location, ResourceBundle resources, Directory data) {
        this.resources = resources;
        this.directory = data;
        this.editableFieldContainer = EditableFieldContainer.create(directory);

        directoryLabel.textProperty().bind(directory.nameProperty());

        if (directory.getImageMetaData().size() > 0) {
            imageCountLabel.setText(TextUtils.format(resources, "editDirectoryController.imageCount.label", directory.getImageMetaData().size()));
        } else {
            imageCountLabel.setText(null);
        }

        pageSizeChoiceBox.setValue(Preferences.getDirectoryTilesPageSize());
        pageSizeChoiceBox.valueProperty().addListener(observable -> {
            this.updateImageOutlet(observable);
            Preferences.setDirectoryTilesPageSize(pageSizeChoiceBox.getValue());
        });
        pagination.setPageCount((int) Math.ceil((double) directory.getImageMetaData().size() / (double) pageSizeChoiceBox.getValue()));
        pagination.currentPageIndexProperty().addListener(this::updateImageOutlet);

        Platform.runLater(() -> updateImageOutlet(null));

        clearImageMetaDataAction.setDisable(data.getImageMetaData().size() == 0);

        // Render editable fields
        final AtomicInteger rowIndexCounter = new AtomicInteger(0);
        editableFieldContainer.getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nKey()));
            final Control control = fieldDefinition.getHandler().getControl();

            label.setPrefHeight(30.0);
            control.setPrefHeight(30.0);

            final int rowIndex = rowIndexCounter.getAndIncrement();
            this.modelFieldGrid.add(label, 0, rowIndex);
            this.modelFieldGrid.add(control, 1, rowIndex);
        });
    }

    public void toolbarBackAction(MouseEvent mouseEvent) {
        Main.switchToScene(DirectoriesController.class);
    }

    public void editMoreButtonAction(ActionEvent actionEvent) {
        final Button source = (Button) actionEvent.getSource();
        final Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        source.getContextMenu().show(source, bounds.getMinX(), bounds.getMinY());
    }

    public void editSaveAction(MouseEvent mouseEvent) {
        for (FieldDefinition field : this.editableFieldContainer.getFields()) {
            if (field.getHandler().isFieldInvalid()) {
                final String fieldName = resources.getString(field.getI18nKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("editDirectoryController.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("editDirectoryController.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getControl(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        dao.save(directory);
        ToastBuilder.create(Main.getPrimaryStage())
                .withMessage(resources.getString("editDirectoryController.toolbarSaveAction.saved"), directory.getName())
                .queue();
    }

    public void editSyncDeletedFiles(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.editSyncDeletedFilesAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.editSyncDeletedFilesAction.warning.context"), directory.getName())
                .show(() -> {
                    AtomicInteger counter = new AtomicInteger(0);
                    TaskQueueBuilder.create(resources)
                            .appendTask(new SyncDeletedFilesTask(directory), counter::addAndGet)
                            .onSucceeded(() -> ToastBuilder.create(Main.getPrimaryStage())
                                    .withMessage(resources.getString("editDirectoryController.editSyncDeletedFilesAction.toast"), counter.get())
                                    .queue())
                            .onSucceeded(() -> pagination.setCurrentPageIndex(0)) // Todo: This reloads???
                            .run();
                });
    }

    public void editClearImageMetaDataAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.editClearImageMetaDataAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.editClearImageMetaDataAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.delete(directory.getImageMetaData());
                    directory.getImageMetaData().clear();

                    ToastBuilder.create(Main.getPrimaryStage())
                            .withMessage(resources.getString("editDirectoryController.clearImageMetaDataAction.toast"), directory.getName())
                            .queue();

                    imageOutlet.getChildren().clear();
                    imageCountLabel.setText(null);
                });
    }

    public void editDeleteDirectoryAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.delete(directory);
                    ToastBuilder.create(Main.getPrimaryStage())
                            .withMessage(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.toast"), directory.getName())
                            .queue();

                    toolbarBackAction(null);
                });
    }

    private void updateImageOutlet(Observable observable) {
        final ObservableList<Node> children = imageOutlet.getChildren();
        final Integer pageSize = pageSizeChoiceBox.getValue();
        final int currentPageIndex = pagination.getCurrentPageIndex();

        children.clear();
        directory.getImageMetaData().stream()
                .skip(currentPageIndex * pageSize)
                .limit(pageSize)
                .map(this::createImageTile)
                .forEach(children::add);

        imageOutletScrollPane.setVvalue(0.0);
    }

    private Parent createImageTile(ImageMetaData imageMetaData) {
        final Scene scene = UIUtils.createScene(ImageTileController.class, imageMetaData);
        return scene.getRoot();
    }
}
