package nl.juraji.imagemanager.fxml.scenes;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.components.builders.AlertBuilder;
import nl.juraji.imagemanager.components.builders.ToastBuilder;
import nl.juraji.imagemanager.fxml.controls.ImageTileController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.SyncDeletedFilesTask;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.InitializableWithData;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;

import java.net.URL;
import java.util.Comparator;
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

        directoryLabel.setText(directory.getName());

        if (directory.getMetaDataCount() > 0) {
            imageCountLabel.setText(TextUtils.format(resources, "editDirectoryController.imageCount.label", directory.getMetaDataCount()));
        } else {
            imageCountLabel.setText(null);
        }

        pageSizeChoiceBox.setValue(Preferences.getDirectoryTilesPageSize());
        pageSizeChoiceBox.valueProperty().addListener(observable ->
                Preferences.setDirectoryTilesPageSize(pageSizeChoiceBox.getValue()));
        pagination.setPageCount((int) Math.ceil((double) directory.getMetaDataCount() / (double) pageSizeChoiceBox.getValue()));
        pagination.currentPageIndexProperty().addListener(this::updateImageOutlet);

        Platform.runLater(() -> {
            dao.load(directory, "imageMetaData");
            updateImageOutlet(null);
        });

        clearImageMetaDataAction.setDisable(data.getMetaDataCount() == 0);

        // Render editable fields
        final AtomicInteger rowIndexCounter = new AtomicInteger(0);
        editableFieldContainer.getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nLabelKey()));
            final Control control = fieldDefinition.getHandler().getControl();

            label.setPrefHeight(30.0);
            control.setPrefHeight(30.0);

            final int rowIndex = rowIndexCounter.getAndIncrement();
            modelFieldGrid.add(label, 0, rowIndex);
            modelFieldGrid.add(control, 1, rowIndex);
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
                final String fieldName = resources.getString(field.getI18nLabelKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("editDirectoryController.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("editDirectoryController.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getTextValue(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        dao.save(directory);
        ToastBuilder.create()
                .withMessage(resources.getString("editDirectoryController.toolbarSaveAction.saved"), directory.getName())
                .show();

        directoryLabel.setText(directory.getName());
    }

    public void editSyncDeletedFiles(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.editSyncDeletedFilesAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.editSyncDeletedFilesAction.warning.context"), directory.getName())
                .show(() -> {
                    try {
                        final AtomicInteger counter = new AtomicInteger(0);
                        TaskQueueBuilder.create(resources)
                                .appendTask(new SyncDeletedFilesTask(directory), counter::addAndGet)
                                .onSucceeded(() -> ToastBuilder.create()
                                        .withMessage(resources.getString("editDirectoryController.editSyncDeletedFilesAction.toast"), counter.get())
                                        .show())
                                .onSucceeded(() -> pagination.setCurrentPageIndex(0)) // Todo: This reloads???
                                .onSucceeded(() -> Main.getPrimaryController().updateStatusBar())
                                .run();
                    } catch (TaskQueueBuilder.TaskInProgressException e) {
                        ToastBuilder.create()
                                .withMessage(resources.getString("tasks.taskInProgress.toast"))
                                .show();
                    }
                });
    }

    public void editClearImageMetaDataAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.editClearImageMetaDataAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.editClearImageMetaDataAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.load(directory, "imageMetaData");
                    dao.delete(directory.getImageMetaData());
                    directory.getImageMetaData().clear();

                    ToastBuilder.create()
                            .withMessage(resources.getString("editDirectoryController.clearImageMetaDataAction.toast"), directory.getName())
                            .show();

                    imageOutlet.getChildren().clear();
                    imageCountLabel.setText(null);

                    Main.getPrimaryController().updateStatusBar();
                });
    }

    public void editDeleteDirectoryAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.warning.title"), directory.getName())
                .withContext(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.delete(directory);
                    ToastBuilder.create()
                            .withMessage(resources.getString("editDirectoryController.toolbarDeleteDirectoryAction.toast"), directory.getName())
                            .show();

                    toolbarBackAction(null);
                    Main.getPrimaryController().updateStatusBar();
                });
    }

    private void updateImageOutlet(Observable observable) {
        final ObservableList<Node> children = imageOutlet.getChildren();
        final Integer pageSize = pageSizeChoiceBox.getValue();
        final int currentPageIndex = pagination.getCurrentPageIndex();

        children.clear();
        directory.getImageMetaData().stream()
                .sorted(Comparator.comparing(ImageMetaData::getDateAdded).reversed())
                .skip(currentPageIndex * pageSize)
                .limit(pageSize)
                .map(ImageTileController::createDefaultTile)
                .forEach(children::add);

        imageOutletScrollPane.setVvalue(0.0);
    }
}
