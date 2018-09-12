package nl.juraji.imagemanager.ui.scenes;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.model.TileData;
import nl.juraji.imagemanager.tasks.SyncDeletedFilesProcess;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.DirectoryTile;
import nl.juraji.imagemanager.ui.components.ImageTile;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.fxevents.VoidChangeListener;
import nl.juraji.imagemanager.util.math.PagingMath;
import nl.juraji.imagemanager.util.ui.modelfields.EditableFieldContainer;
import nl.juraji.imagemanager.util.ui.modelfields.FieldDefinition;
import nl.juraji.imagemanager.util.ui.traits.BorderPaneScene;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public class DirectoryScene extends BorderPaneScene {
    private final Dao dao = new Dao();

    private final Directory directory;
    private final EditableFieldContainer editableFieldContainer;

    @FXML
    private Button saveButton;
    @FXML
    private MenuItem clearImageMetaDataAction;

    @FXML
    private Label directoryLabel;
    @FXML
    private Label subDirectoryCountLabel;
    @FXML
    private Label imageCountLabel;

    @FXML
    private Pagination pagination;
    @FXML
    private Label paginationPageInformationLabel;
    @FXML
    private ChoiceBox<Integer> pageSizeChoiceBox;
    @FXML
    private ScrollPane imageOutletScrollPane;
    @FXML
    private TilePane imageOutlet;

    @FXML
    private GridPane modelFieldGrid;

    public DirectoryScene(Directory directory) {
        this.directory = directory;
        this.editableFieldContainer = EditableFieldContainer.create(directory);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        directoryLabel.setText(directory.getName());

        if (directory.getMetaDataCount() > 0) {
            imageCountLabel.setText(TextUtils.format(resources, "DirectoryScene.imageCount.label", directory.getMetaDataCount()));
        } else {
            imageCountLabel.setText(null);
        }

        if (directory.getSubDirectoryCount() > 0) {
            subDirectoryCountLabel.setText(TextUtils.format(resources, "DirectoryScene.subDirectoryCount.label", directory.getSubDirectoryCount()));
        } else {
            subDirectoryCountLabel.setText(null);
        }

        pageSizeChoiceBox.setValue(Preferences.Scenes.EditDirectory.getPageSize());
        pageSizeChoiceBox.valueProperty().addListener(observable -> {
            Preferences.Scenes.EditDirectory.setPageSize(pageSizeChoiceBox.getValue());
            pagination.setPageCount(PagingMath.pageCount(directory.getMetaDataCount(), pageSizeChoiceBox.getValue()));
            this.updateImageOutlet(false);
        });

        pagination.currentPageIndexProperty().addListener((VoidChangeListener) this::updateImageOutlet);
        pagination.setPageCount(PagingMath.pageCount(directory.getMetaDataCount(), pageSizeChoiceBox.getValue()));
        paginationPageInformationLabel.textProperty().bind(pagination.currentPageIndexProperty()
                .add(1).asString().concat("/").concat(pagination.getPageCount()));

        clearImageMetaDataAction.setDisable(directory.getMetaDataCount() == 0);

        // Render editable fields
        final AtomicInteger rowIndexCounter = new AtomicInteger(0);
        modelFieldGrid.getChildren().clear();
        editableFieldContainer.getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nLabelKey()));
            final Control control = fieldDefinition.getHandler().getControl();

            label.setPrefHeight(30.0);
            control.setPrefHeight(30.0);

            final int rowIndex = rowIndexCounter.getAndIncrement();
            modelFieldGrid.addRow(rowIndex, label, control);
        });
    }

    @Override
    public void postInitialization() {
        this.updateImageOutlet();
    }

    @Override
    public void postReloadedInView() {
        dao.refresh(this.directory);
        this.updateImageOutlet();
    }

    @FXML
    private void toolbarBackAction(MouseEvent mouseEvent) {
        Main.getPrimaryScene().previousContent();
    }

    @FXML
    private void editMoreButtonAction(ActionEvent actionEvent) {
        final Button source = (Button) actionEvent.getSource();
        final Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        source.getContextMenu().show(source, bounds.getMinX(), bounds.getMinY());
    }

    @FXML
    private void editSaveAction(MouseEvent mouseEvent) {
        for (FieldDefinition field : this.editableFieldContainer.getFields()) {
            if (field.getHandler().isFieldInvalid()) {
                final String fieldName = resources.getString(field.getI18nLabelKey());
                AlertBuilder.createWarning()
                        .withTitle(resources.getString("DirectoryScene.toolbarSaveAction.fieldInvalid.title"), fieldName)
                        .withContext(resources.getString("DirectoryScene.toolbarSaveAction.fieldInvalid.context"),
                                field.getHandler().getTextValue(), fieldName)
                        .show();
                return;
            }
        }

        // Save changes
        dao.save(directory);
        ToastBuilder.create()
                .withMessage(resources.getString("DirectoryScene.toolbarSaveAction.saved"), directory.getName())
                .show();

        directoryLabel.setText(directory.getName());
    }

    @FXML
    private void editSyncDeletedFiles(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("DirectoryScene.editSyncDeletedFilesAction.warning.title"), directory.getName())
                .withContext(resources.getString("DirectoryScene.editSyncDeletedFilesAction.warning.context"), directory.getName())
                .show(() -> Main.getPrimaryScene().getProcessExecutor().submitProcess(new SyncDeletedFilesProcess(directory), deletedCount -> {
                    ToastBuilder.create()
                            .withMessage(resources.getString("DirectoryScene.editSyncDeletedFilesAction.toast"), deletedCount)
                            .show();

                    // Refresh view
                    dao.refresh(directory);
                    updateImageOutlet();

                    Main.getPrimaryScene().updateStatusBar();
                }));
    }

    @FXML
    private void editClearImageMetaDataAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("DirectoryScene.editClearImageMetaDataAction.warning.title"), directory.getName())
                .withContext(resources.getString("DirectoryScene.editClearImageMetaDataAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.delete(directory.getImageMetaData());
                    directory.getImageMetaData().clear();

                    ToastBuilder.create()
                            .withMessage(resources.getString("DirectoryScene.clearImageMetaDataAction.toast"), directory.getName())
                            .show();

                    this.postReloadedInView();
                    Main.getPrimaryScene().updateStatusBar();
                });
    }

    @FXML
    private void editDeleteDirectoryAction(ActionEvent mouseEvent) {
        AlertBuilder.createConfirm()
                .withTitle(resources.getString("DirectoryScene.toolbarDeleteDirectoryAction.warning.title"), directory.getName())
                .withContext(resources.getString("DirectoryScene.toolbarDeleteDirectoryAction.warning.context"), directory.getName())
                .show(() -> {
                    dao.delete(directory);
                    ToastBuilder.create()
                            .withMessage(resources.getString("DirectoryScene.toolbarDeleteDirectoryAction.toast"), directory.getName())
                            .show();

                    toolbarBackAction(null);
                    Main.getPrimaryScene().updateStatusBar();
                });
    }

    private void updateImageOutlet() {
        this.updateImageOutlet(true);
    }

    private void updateImageOutlet(boolean resetScroll) {
        final ObservableList<Node> children = imageOutlet.getChildren();
        final Integer pageSize = pageSizeChoiceBox.getValue();
        final int currentPageIndex = pagination.getCurrentPageIndex();

        children.clear();
        final List<? extends TileData> directories = directory.getDirectories();
        final List<? extends TileData> metaData = directory.getImageMetaData();

        Stream.concat(directories.stream(), metaData.stream())
                .skip(currentPageIndex * pageSize)
                .limit(pageSize)
                .map(tileData -> {
                    if (tileData instanceof Directory) {
                        return new DirectoryTile(this.imageOutlet, (Directory) tileData);
                    } else {
                        return new ImageTile(this.imageOutlet, (ImageMetaData) tileData, directory.getImageMetaData());
                    }
                })
                .forEach(children::add);

        if (resetScroll) {
            imageOutletScrollPane.setVvalue(0.0);
        }
    }
}
