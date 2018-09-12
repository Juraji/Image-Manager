package nl.juraji.imagemanager.ui.scenes;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.tasks.DuplicateScanProcess;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.ChoiceProperty;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.ImageTile;
import nl.juraji.imagemanager.util.concurrent.ProcessExecutor;
import nl.juraji.imagemanager.util.fxevents.ValueChangeListener;
import nl.juraji.imagemanager.util.ui.modifiers.DuplicateSetCellFactory;
import nl.juraji.imagemanager.util.ui.traits.BorderPaneScene;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static nl.juraji.imagemanager.tasks.DuplicateScanProcess.ScanType.FULL_SCAN;
import static nl.juraji.imagemanager.tasks.DuplicateScanProcess.ScanType.PER_DIRECTORY_SCAN;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public class DuplicateScanScene extends BorderPaneScene {
    private final Dao dao = new Dao();
    private DuplicateScanProcess.DuplicateSet currentSet;

    @FXML
    private ListView<DuplicateScanProcess.DuplicateSet> duplicateSetListView;
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
                (ValueChangeListener<DuplicateScanProcess.DuplicateSet>) this::duplicateSetSelectedHandler);
    }

    private void runScanForType(ChoiceProperty<DuplicateScanProcess.ScanType> choice) {
        duplicateSetListView.getItems().clear();
        imageOutlet.getChildren().clear();

        switch (choice.getValue()) {
            case PER_DIRECTORY_SCAN:
                this.runScanPerDirectory();
                break;
            case FULL_SCAN:
                this.runScanFull();
                break;
        }
    }

    private void runScanPerDirectory() {
        duplicateSetViewToolbar.setDisable(true);
        duplicateSetListView.getItems().clear();

        final List<Directory> directories = dao.getAllDirectories();

        final ProcessExecutor processExecutor = Main.getPrimaryScene().getProcessExecutor();
        directories.forEach(directory -> processExecutor.submitProcess(new DuplicateScanProcess(directory), this::scanResultHandler));
    }

    private void runScanFull() {
        duplicateSetViewToolbar.setDisable(true);
        final List<ImageMetaData> imageMetaData = dao.getAllImageMetaData();
        final Directory tempDirectory = new Directory();
        tempDirectory.setName("All directories"); // Todo i18n
        tempDirectory.getImageMetaData().addAll(imageMetaData);

        Main.getPrimaryScene().getProcessExecutor().submitProcess(new DuplicateScanProcess(tempDirectory), this::scanResultHandler);
    }

    private void scanResultHandler(List<DuplicateScanProcess.DuplicateSet> duplicateSets) {
        duplicateSets.forEach(duplicateSet -> duplicateSetListView.getItems().add(duplicateSet));
        duplicateSetViewToolbar.setDisable(false);
    }

    private void duplicateSetSelectedHandler(DuplicateScanProcess.DuplicateSet newValue) {
        final ObservableList<Node> children = imageOutlet.getChildren();

        children.clear();
        currentSet = newValue;

        if (currentSet == null) {
            duplicateSetViewToolbar.setDisable(true);
        } else {
            duplicateSetViewToolbar.setDisable(false);
            newValue.getImageMetaData().stream()
                    .sorted(Comparator.comparingLong(ImageMetaData::getQualityRating).reversed())
                    .map(i -> new ImageTile(imageOutlet, i))
                    .forEach(children::add);
        }

        imageOutletScrollPane.setVvalue(0.0);
    }

    @FXML
    private void toolbarBackAction() {
        Main.getPrimaryScene().previousContent();
    }

    @FXML
    private void toolbarRunScansAction() {
        final ArrayList<ChoiceProperty<DuplicateScanProcess.ScanType>> list = new ArrayList<>();
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.perDirectory"), PER_DIRECTORY_SCAN));
        list.add(new ChoiceProperty<>(resources.getString("duplicateScanTypes.fullScan"), FULL_SCAN));

        final ChoiceDialog<ChoiceProperty<DuplicateScanProcess.ScanType>> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(resources.getString("DuplicateScanScene.toolbar.runScansAction.dialog.title"));
        dialog.setHeaderText(null);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(resources
                .getString("DuplicateScanScene.toolbar.runScansAction.dialog.startButton.label"));
        dialog.showAndWait().ifPresent(this::runScanForType);
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
