package nl.juraji.imagemanager.ui.scenes;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.DuplicateScanTask;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ChoiceProperty;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.ImageTile;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.events.ValueChangeListener;
import nl.juraji.imagemanager.util.ui.modifiers.DuplicateSetCellFactory;
import nl.juraji.imagemanager.util.ui.traits.BorderPaneScene;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static nl.juraji.imagemanager.tasks.DuplicateScanTask.ScanType.FULL_SCAN;
import static nl.juraji.imagemanager.tasks.DuplicateScanTask.ScanType.PER_DIRECTORY_SCAN;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public class DuplicateScanScene extends BorderPaneScene {
    private final Dao dao = new Dao();
    private DuplicateScanTask.DuplicateSet currentSet;

    @FXML
    private ListView<DuplicateScanTask.DuplicateSet> duplicateSetListView;
    @FXML
    private ScrollPane imageOutletScrollPane;
    @FXML
    private ToolBar duplicateSetViewToolbar;
    @FXML
    private TilePane imageOutlet;

    public DuplicateScanScene() {
        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        duplicateSetListView.setCellFactory(new DuplicateSetCellFactory());
        duplicateSetListView.getSelectionModel().selectedItemProperty().addListener(
                (ValueChangeListener<DuplicateScanTask.DuplicateSet>) this::duplicateSetSelectedHandler);
    }

    @FXML
    private void toolbarBackAction(MouseEvent mouseEvent) {
        mouseEvent.consume();
        Main.getPrimaryScene().previousContent();
    }

    @FXML
    private void toolbarRunScansAction(MouseEvent mouseEvent) {
        mouseEvent.consume();
        final ArrayList<ChoiceProperty<DuplicateScanTask.ScanType>> list = new ArrayList<>();
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.perDirectory"), PER_DIRECTORY_SCAN));
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.fullScan"), FULL_SCAN));

        final ChoiceDialog<ChoiceProperty<DuplicateScanTask.ScanType>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("DuplicateScanScene.toolbar.runScansAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources
                .getString("DuplicateScanScene.toolbar.runScansAction.dialog.startButton.label"));
        dialog.showAndWait().ifPresent(this::runScanForType);
    }

    private void runScanForType(ChoiceProperty<DuplicateScanTask.ScanType> choice) {
        duplicateSetListView.getItems().clear();
        imageOutlet.getChildren().clear();

        try {
            switch (choice.getValue()) {
                case PER_DIRECTORY_SCAN:
                    this.runScanPerDirectory();
                    break;
                case FULL_SCAN:
                    this.runScanFull();
                    break;
            }
        } catch (TaskQueueBuilder.TaskInProgressException e) {
            ToastBuilder.create()
                    .withMessage(resources.getString("tasks.taskInProgress.toast"))
                    .show();
        }
    }

    private void runScanPerDirectory() throws TaskQueueBuilder.TaskInProgressException {
        duplicateSetViewToolbar.setDisable(true);
        duplicateSetListView.getItems().clear();

        final List<Directory> directories = dao.getAllDirectories();

        TaskQueueBuilder queueBuilder = TaskQueueBuilder.create(resources);
        directories.forEach(directory -> queueBuilder.appendTask(new DuplicateScanTask(directory), this::scanResultHandler));
        queueBuilder.onSucceeded(() -> duplicateSetViewToolbar.setDisable(false));
        queueBuilder.run();


    }

    private void runScanFull() throws TaskQueueBuilder.TaskInProgressException {
        duplicateSetViewToolbar.setDisable(true);
        final List<ImageMetaData> imageMetaData = dao.getAllImageMetaData();
        final Directory tempDirectory = new Directory();
        tempDirectory.setName("All directories"); // Todo i18n
        tempDirectory.getImageMetaData().addAll(imageMetaData);

        TaskQueueBuilder.create(resources)
                .appendTask(new DuplicateScanTask(tempDirectory), this::scanResultHandler)
                .onSucceeded(() -> duplicateSetViewToolbar.setDisable(false))
                .run();
    }

    private void scanResultHandler(List<DuplicateScanTask.DuplicateSet> duplicateSets) {
        duplicateSets.forEach(duplicateSet -> duplicateSetListView.getItems().add(duplicateSet));
    }

    private void duplicateSetSelectedHandler(DuplicateScanTask.DuplicateSet newValue) {
        final ObservableList<Node> children = imageOutlet.getChildren();

        children.clear();
        currentSet = newValue;

        if (currentSet == null) {
            duplicateSetViewToolbar.setDisable(true);
        } else {
            duplicateSetViewToolbar.setDisable(false);
            newValue.getImageMetaData().stream()
                    .sorted(Comparator.comparingLong(ImageMetaData::getQualityRating).reversed())
                    .map(ImageTile::new)
                    .forEach(children::add);
        }

        imageOutletScrollPane.setVvalue(0.0);
    }

    @FXML
    private void duplicateSetViewToolbarDoneAction() {
        if (currentSet != null) {
            duplicateSetListView.getItems().remove(currentSet);
        }
    }

    @FXML
    private void duplicateSetViewToolbarRemoveWorstAction() {
        if (currentSet != null) {
            final int deleteCount = currentSet.getImageMetaData().size() - 1;
            AlertBuilder.createConfirm()
                    .withTitle(resources.getString("DuplicateScanScene.duplicateSetView.toolbar.removeWorstAction.confirm.title"), deleteCount)
                    .withContext(resources.getString("DuplicateScanScene.duplicateSetView.toolbar.removeWorstAction.confirm.context"), deleteCount)
                    .show(() -> {
                        currentSet.getImageMetaData().stream()
                                .sorted(Comparator.comparingLong(ImageMetaData::getQualityRating).reversed())
                                .skip(1)
                                .forEach(imageMetaData -> {
                                    try {
                                        Files.deleteIfExists(imageMetaData.getFile().toPath());
                                        dao.delete(imageMetaData);

                                        Main.getPrimaryScene().updateStatusBar();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                        duplicateSetSelectedHandler(currentSet);
                        ToastBuilder.create()
                                .withMessage(resources.getString("DuplicateScanScene.duplicateSetView.toolbar.removeWorstAction.deleted.toast"), deleteCount)
                                .show();
                    });
        }
    }
}
