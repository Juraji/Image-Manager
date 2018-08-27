package nl.juraji.imagemanager.fxml.scenes;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.dialogs.AlertBuilder;
import nl.juraji.imagemanager.dialogs.ToastBuilder;
import nl.juraji.imagemanager.fxml.controls.ImageTileController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.DuplicateScanTask;
import nl.juraji.imagemanager.tasks.DuplicateScanTask.DuplicateSet;
import nl.juraji.imagemanager.tasks.DuplicateScanTask.ScanType;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.cellfactories.DuplicateSetCellFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

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
        final List<ScanType> types = ScanType.getTypes(resources);
        final ChoiceDialog<ScanType> dialog = new ChoiceDialog<>(types.get(0), types);

        dialog.setHeaderText(null);
        dialog.setTitle(resources.getString("duplicateScansController.toolbar.runScansAction.dialog.title"));
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources.getString("duplicateScansController.toolbar.runScansAction.dialog.startButton.label"));
        dialog.showAndWait().ifPresent(this::runScanForType);
    }

    private void runScanForType(ScanType scanType) {
        switch (scanType.getType()) {
            case PER_DIRECTORY_SCAN:
                this.runScanPerDirectory();
                break;
            case FULL_SCAN:
                this.runScanFull();
                break;
        }
    }

    private void runScanPerDirectory() {
        duplicateSetListView.getItems().clear();
        final List<Directory> directories = new Dao().get(Directory.class);

        final TaskQueueBuilder queueBuilder = TaskQueueBuilder.create(resources);
        directories.forEach(directory -> queueBuilder.appendTask(new DuplicateScanTask(directory), this::intermediateScanResultHandler));

        queueBuilder.run();

    }

    private void runScanFull() {
        final List<ImageMetaData> imageMetaData = new Dao().get(ImageMetaData.class);
        final Directory tempDirectory = new Directory();
        tempDirectory.setName("All directories"); // Todo i18n
        tempDirectory.getImageMetaData().addAll(imageMetaData);

        TaskQueueBuilder.create(resources)
                .appendTask(new DuplicateScanTask(tempDirectory), this::intermediateScanResultHandler)
                .run();
    }

    private void intermediateScanResultHandler(List<DuplicateSet> duplicateSets) {
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

    private Parent createImageTile(ImageMetaData imageMetaData) {
        final Scene scene = UIUtils.createScene(ImageTileController.class, imageMetaData);
        return scene.getRoot();
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
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });

                        duplicateSetSelectedHandler(null, currentSet, currentSet);
                        ToastBuilder.create(Main.getPrimaryStage())
                                .withMessage(resources.getString("duplicateScansController.duplicateSetView.toolbar.removeWorstAction.deleted.toast"), deleteCount)
                                .queue();
                    });
        }
    }
}
