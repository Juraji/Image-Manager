package nl.juraji.imagemanager.ui.scenes;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.TilePane;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.tasks.*;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.DirectoryChooserBuilder;
import nl.juraji.imagemanager.ui.builders.PinterestBoardChooserBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.components.DirectoryTile;
import nl.juraji.imagemanager.util.concurrent.ProcessChainBuilder;
import nl.juraji.imagemanager.util.fxevents.AcceleratorMap;
import nl.juraji.imagemanager.util.ui.traits.BorderPaneScene;

import javax.security.auth.login.CredentialException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by Juraji on 11-9-2018.
 * Image Manager
 */
public class RootDirectoryScene extends BorderPaneScene {

    private final Dao dao;

    @FXML
    private ScrollPane directoryOutletScrollPane;
    @FXML
    private TilePane directoryOutlet;

    public RootDirectoryScene() {
        this.dao = new Dao();
        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        this.loadDirectories();
    }

    @Override
    public void postReloadedInView() {
        this.loadDirectories();
    }

    @Override
    public AcceleratorMap getAccelerators() {
        return new AcceleratorMap()
                .putKeyWithControl(KeyCode.D, this::toolbarDuplicateScannerAction, "RootDirectoryScene.accelerators.toolbarDuplicateScannerAction.name");
    }

    @FXML
    private void toolbarDuplicateScannerAction() {
        Main.getPrimaryScene().pushContent(new DuplicateScanScene());
    }

    @FXML
    private void toolbarSettingsAction() {
        Main.getPrimaryScene().pushContent(new SettingsScene());
    }

    @FXML
    private void toolbarAddMenuAddDirectoryAction() {
        DirectoryChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("RootDirectoryScene.menuAddDirectoryAction.directoryChooser.title"))
                .show(f -> {
                    final Directory directory = new Directory();
                    directory.setName(f.getName());
                    directory.setTargetLocation(f);

                    dao.save(directory);

                    ToastBuilder.create()
                            .withMessage(resources.getString("RootDirectoryScene.menuAddDirectoryAction.toast"), f.getAbsolutePath())
                            .show();

                    // Reload the directories in order to keep persistence sane
                    this.loadDirectories();
                    Main.getPrimaryScene().updateStatusBar();
                });
    }

    @FXML
    private void toolbarAddMenuAddPinterestBoardsAction() {
        Consumer<List<PinterestBoard>> selectedBoardsHandler = result -> PinterestBoardChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("RootDirectoryScene.menuAddAddPinterestBoardsAction.selectBoards.title"))
                .withPinterestBoards(result)
                .showAndWait()
                .ifPresent(selected -> {
                    new Dao().save(selected);

                    // Reload the directories in order to keep persistence sane
                    this.loadDirectories();
                    Main.getPrimaryScene().updateStatusBar();
                });

        Consumer<Throwable> exceptionHandler = e -> ToastBuilder.create()
                .withMessage("An error occurred while fetching Pinterest boards, try again later.\nError details: " + e.getMessage())
                .show();

        try {
            ProcessChainBuilder.create(resources)
                    .appendTask(new FindPinterestBoardsProcess(), selectedBoardsHandler, exceptionHandler)
                    .run();
        } catch (CredentialException e) {
            AlertBuilder.createWarning()
                    .withTitle("No login set for Pinterest service")
                    .withContext("You haven't yet set any pinterest authentication information.\nDo so by going to File -> Settings and fill out the form under Pinterest Settings.")
                    .show();
        } catch (ProcessChainBuilder.TaskInProgressException e) {
            ToastBuilder.create()
                    .withMessage(resources.getString("tasks.taskInProgress.toast"))
                    .show();
        }
    }

    @FXML
    private void refreshAllDirectoriesAction() {
        final List<Directory> directories = dao.getRootDirectories();

        try {
            ToastBuilder.create()
                    .withMessage(resources.getString("RootDirectoryScene.refreshAllDirectoriesAction.running.toast"))
                    .show();

            final ProcessChainBuilder builder = ProcessChainBuilder.create(resources);

            for (Directory directory : directories) {
                builder
                        .appendTask(DirectoryScanners.forDirectory(directory))
                        .appendTask(new DownloadImagesProcess(directory))
                        .appendTask(new CorrectImageTypesProcess(directory))
                        .appendTask(new BuildHashesProcess(directory));
            }

            builder
                    .onSucceeded(() -> ToastBuilder.create()
                            .withMessage(resources.getString("RootDirectoryScene.refreshAllDirectoriesAction.completed.toast"), directories.size())
                            .show())
                    .onSucceeded(this::loadDirectories)
                    .onSucceeded(() -> Main.getPrimaryScene().updateStatusBar())
                    .run();
        } catch (ProcessChainBuilder.TaskInProgressException e) {
            ToastBuilder.create()
                    .withMessage(resources.getString("tasks.taskInProgress.toast"))
                    .show();
        }
    }

    private void loadDirectories() {
        final List<Directory> rootDirectories = dao.getRootDirectories();
        final ObservableList<Node> children = directoryOutlet.getChildren();

        children.clear();
        rootDirectories.stream()
                .map(directory -> new DirectoryTile(this.directoryOutlet, directory))
                .forEach(children::add);

        directoryOutletScrollPane.setVvalue(0.0);
    }
}
