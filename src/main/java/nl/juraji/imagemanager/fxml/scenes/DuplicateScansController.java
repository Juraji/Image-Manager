package nl.juraji.imagemanager.fxml.scenes;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.fxml.controls.ImageTileController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.DuplicateScanTask;
import nl.juraji.imagemanager.tasks.DuplicateScanTask.DuplicateSet;
import nl.juraji.imagemanager.tasks.DuplicateScanTask.ScanType;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.AlertBuilder;
import nl.juraji.imagemanager.util.ui.ChoiceProperty;
import nl.juraji.imagemanager.util.ui.ToastBuilder;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.cellfactories.DuplicateSetCellFactory;

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
 * Created by Juraji on 26-8-2018.
 * Image Manager
 */
public class DuplicateScansController implements Initializable {
    private ResourceBundle resources;
    private DuplicateSet currentSet;

    public ListView<DuplicateSet> duplicateSetListView;
    public ScrollPane imageOutletScrollPane;
    public ToolBar duplicateSetViewToolbar;
    public TilePane imageOutlet;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        duplicateSetListView.setCellFactory(new DuplicateSetCellFactory());
        duplicateSetListView.getSelectionModel().selectedItemProperty().addListener(this::duplicateSetSelectedHandler);
    }

    public void toolbarBackAction(MouseEvent mouseEvent) {
        Main.switchToScene(DirectoriesController.class);
    }

    public void toolbarRunScansAction(MouseEvent mouseEvent) {
        final ArrayList<ChoiceProperty<ScanType>> list = new ArrayList<>();
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.perDirectory"), PER_DIRECTORY_SCAN));
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.fullScan"), FULL_SCAN));

        final ChoiceDialog<ChoiceProperty<ScanType>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("duplicateScansController.toolbar.runScansAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources.getString("duplicateScansController.toolbar.runScansAction.dialog.startButton.label"));
        dialog.showAndWait().ifPresent(this::runScanForType);
    }

    private void runScanForType(ChoiceProperty<ScanType> choice) {
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
        duplicateSetListView.getItems().clear();
        final List<Directory> directories = new Dao().get(Directory.class);

        TaskQueueBuilder queueBuilder = TaskQueueBuilder.create();
        directories.forEach(directory -> queueBuilder.appendTask(new DuplicateScanTask(directory), this::scanResultHandler));
        queueBuilder.run();


    }

    private void runScanFull() throws TaskQueueBuilder.TaskInProgressException {
        final List<ImageMetaData> imageMetaData = new Dao().get(ImageMetaData.class);
        final Directory tempDirectory = new Directory();
        tempDirectory.setName("All directories"); // Todo i18n
        tempDirectory.getImageMetaData().addAll(imageMetaData);

        TaskQueueBuilder.create()
                .appendTask(new DuplicateScanTask(tempDirectory), this::scanResultHandler)
                .run();
    }

    private void scanResultHandler(List<DuplicateSet> duplicateSets) {
        duplicateSets.forEach(duplicateSet -> duplicateSetListView.getItems().add(duplicateSet));
    }

    private void duplicateSetSelectedHandler(ObservableValue<? extends DuplicateSet> observable, DuplicateSet oldValue, DuplicateSet newValue) {
        final ObservableList<Node> children = imageOutlet.getChildren();

        children.clear();
        currentSet = newValue;

        if (currentSet == null) {
            duplicateSetViewToolbar.setDisable(true);
        } else {
            duplicateSetViewToolbar.setDisable(false);
            newValue.getImageMetaData().stream()
                    .sorted(Comparator.comparingLong(ImageMetaData::getQualityRating).reversed())
                    .map(this::createImageTile)
                    .forEach(children::add);
        }

        imageOutletScrollPane.setVvalue(0.0);
    }

    private Node createImageTile(ImageMetaData imageMetaData) {
        return UIUtils.createView(ImageTileController.class, imageMetaData);
    }

    public void duplicateSetViewToolbarDoneAction(MouseEvent mouseEvent) {
        if (currentSet != null) {
            duplicateSetListView.getItems().remove(currentSet);
        }
    }

    public void duplicateSetViewToolbarRemoveWorstAction(MouseEvent mouseEvent) {
        if (currentSet != null) {
            final int deleteCount = currentSet.getImageMetaData().size() - 1;
            AlertBuilder.createConfirm()
                    .withTitle(resources.getString("duplicateScansController.duplicateSetView.toolbar.removeWorstAction.confirm.title"), deleteCount)
                    .withContext(resources.getString("duplicateScansController.duplicateSetView.toolbar.removeWorstAction.confirm.context"), deleteCount)
                    .show(() -> {
                        final Dao dao = new Dao();
                        currentSet.getImageMetaData().stream()
                                .sorted(Comparator.comparingLong(ImageMetaData::getQualityRating).reversed())
                                .skip(1)
                                .forEach(imageMetaData -> {
                                    try {
                                        Files.deleteIfExists(imageMetaData.getFile().toPath());
                                        dao.delete(imageMetaData);

                                        Main.getPrimaryController().updateStatusBar();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                        duplicateSetSelectedHandler(null, currentSet, currentSet);
                        ToastBuilder.create()
                                .withMessage(resources.getString("duplicateScansController.duplicateSetView.toolbar.removeWorstAction.deleted.toast"), deleteCount)
                                .show();
                    });
        }
    }
}
