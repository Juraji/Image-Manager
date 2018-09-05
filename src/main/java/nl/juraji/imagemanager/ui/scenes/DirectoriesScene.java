package nl.juraji.imagemanager.ui.scenes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.tasks.*;
import nl.juraji.imagemanager.ui.builders.AlertBuilder;
import nl.juraji.imagemanager.ui.builders.DirectoryChooserBuilder;
import nl.juraji.imagemanager.ui.builders.PinterestBoardChooserBuilder;
import nl.juraji.imagemanager.ui.builders.ToastBuilder;
import nl.juraji.imagemanager.ui.util.BorderPaneScene;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.UIUtils;

import javax.security.auth.login.CredentialException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by Juraji on 4-9-2018.
 * Image Manager
 */
public class DirectoriesScene extends BorderPaneScene {
    private final ObservableList<Directory> directoryTableModel;
    private final Dao dao;

    @FXML
    public TableView<Directory> directoryTable;

    public DirectoriesScene() {
        this.directoryTableModel = FXCollections.observableArrayList();
        this.dao = new Dao();

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        // Load preferences for directoryTable and add change listeners to persist settings
        directoryTable.getColumns().forEach(column -> {
            final Double columnWidth = Preferences.getColumnWidth(column.getId());
            if (columnWidth != null) {
                column.setPrefWidth(columnWidth);
            }

            final boolean columnVisible = Preferences.getColumnVisible(column.getId());
            column.setVisible(columnVisible);

            column.widthProperty().addListener((observable, oldValue, newValue) ->
                    Preferences.setColumnWidth(column.getId(), newValue));

            column.visibleProperty().addListener((observable, oldValue, newValue) ->
                    Preferences.setColumnVisible(column.getId(), newValue));
        });

        // UI Setup
        directoryTable.setItems(directoryTableModel);
        directoryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Populate directory table
        final List<Directory> directories = dao.get(Directory.class);
        this.directoryTableModel.addAll(directories);

        // Set default sorting
        final TableColumn<Directory, ?> favoriteColumn = directoryTable.getColumns().get(0);
        final TableColumn<Directory, ?> nameColumn = directoryTable.getColumns().get(1);

        favoriteColumn.setSortType(TableColumn.SortType.DESCENDING);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);

        //noinspection unchecked
        directoryTable.getSortOrder().setAll(favoriteColumn, nameColumn);
    }

    @FXML
    private void menuFileSettingsAction(ActionEvent actionEvent) {
        Main.getPrimaryScene().pushContent(new SettingsScene());
    }

    @FXML
    private void menuFileExitApplicationAction(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void menuAddAddDirectoryAction(ActionEvent e) {
        DirectoryChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("DirectoriesScene.menuAddDirectoryAction.directoryChooser.title"))
                .show(f -> {
                    final Directory directory = new Directory();
                    directory.setName(f.getName());
                    directory.setTargetLocation(f);

                    dao.save(directory);
                    directoryTableModel.add(directory);

                    ToastBuilder.create()
                            .withMessage(resources.getString("DirectoriesScene.menuAddDirectoryAction.toast"), f.getAbsolutePath())
                            .show();

                    directoryTable.getSelectionModel().clearSelection();
                    directoryTable.getSelectionModel().select(directory);
                    Main.getPrimaryScene().updateStatusBar();
                });
    }

    @FXML
    private void menuAddAddPinterestBoardsAction(ActionEvent actionEvent) {
        Consumer<List<PinterestBoard>> selectedBoardsHandler = result -> PinterestBoardChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("DirectoriesScene.menuAddAddPinterestBoardsAction.selectBoards.title"))
                .withPinterestBoards(result)
                .showAndWait()
                .ifPresent(selected -> {
                    new Dao().save(selected);

                    directoryTableModel.addAll(selected);
                    directoryTable.getSelectionModel().clearSelection();
                    selected.forEach(board -> directoryTable.getSelectionModel().select(board));
                    Main.getPrimaryScene().updateStatusBar();
                });

        Consumer<Throwable> exceptionHandler = e -> ToastBuilder.create()
                .withMessage("An error occurred while fetching Pinterest boards, try again later.\nError details: " + e.getMessage())
                .show();

        try {
            TaskQueueBuilder.create(resources)
                    .appendTask(new FindPinterestBoardsTask(), selectedBoardsHandler, exceptionHandler)
                    .run();
        } catch (CredentialException e) {
            AlertBuilder.createWarning()
                    .withTitle("No login set for Pinterest service")
                    .withContext("You haven't yet set any pinterest authentication information.\nDo so by going to File -> Settings and fill out the form under Pinterest Settings.")
                    .show();
        } catch (TaskQueueBuilder.TaskInProgressException e) {
            ToastBuilder.create()
                    .withMessage(resources.getString("tasks.taskInProgress.toast"))
                    .show();
        }

    }

    @FXML
    private void menuEditRefreshImageMetaDataAction(ActionEvent actionEvent) {
        final List<Directory> directories = getSelectedItems();

        if (directories.size() > 0) {
            final TaskQueueBuilder queueBuilder;
            try {
                queueBuilder = TaskQueueBuilder.create(resources);
                ToastBuilder.create()
                        .withMessage(resources.getString("DirectoriesScene.refreshMetaDataAction.running.toast"), directories.size())
                        .show();

                for (Directory directory : directories) {
                    queueBuilder
                            .appendTask(DirectoryScanners.forDirectory(directory), o -> directoryTable.refresh())
                            .appendTask(new DownloadImagesTask(directory))
                            .appendTask(new CorrectImageTypesTask(directory))
                            .appendTask(new BuildHashesTask(directory))
                    ;
                }

                queueBuilder
                        .onSucceeded(() -> ToastBuilder.create()
                                .withMessage(resources.getString("DirectoriesScene.refreshMetaDataAction.completed.toast"), directories.size())
                                .show())
                        .onSucceeded(() -> Main.getPrimaryScene().updateStatusBar())
                        .run();
            } catch (TaskQueueBuilder.TaskInProgressException e) {
                ToastBuilder.create()
                        .withMessage(resources.getString("tasks.taskInProgress.toast"))
                        .show();
            }
        }
    }

    @FXML
    private void menuEditDirectoryAction(ActionEvent actionEvent) {
        final Directory item = getLastSelectedItem();

        if (item != null) {
            Main.getPrimaryScene().pushContent(new EditDirectoryScene(item));
        }
    }

    @FXML
    private void menuEditDeleteDirectoriesAction(ActionEvent actionEvent) {
        final List<Directory> items = getSelectedItems();
        if (items.size() > 0) {
            final int itemCount = items.size();
            AlertBuilder.createConfirm()
                    .withTitle(resources.getString("DirectoriesScene.deleteDirectoriesAction.warning.title"), itemCount)
                    .withContext(resources.getString("DirectoriesScene.deleteDirectoriesAction.warning.context"))
                    .show(() -> {
                        dao.delete(items);

                        ToastBuilder.create()
                                .withMessage(resources.getString("DirectoriesScene.deleteDirectoriesAction.toast"), itemCount)
                                .show();

                        Main.getPrimaryScene().updateStatusBar();
                        Main.getPrimaryScene().pushContent(new DirectoriesScene(), true);
                    });
        }
    }

    @FXML
    private void menuScannersDuplicateScannerAction(ActionEvent actionEvent) {
        Main.getPrimaryScene().pushContent(new DuplicateScanScene());
    }

    @FXML
    private void menuHelpAboutAction(ActionEvent actionEvent) {
        AlertBuilder.createInfo()
                .withTitle(resources.getString("DirectoriesScene.menuAboutAction.title"))
                .withContext("Image Manager 1.0.0\n© Juraji {}\n{}\nGithub: {}",
                        LocalDate.now().getYear(), "https://juraji.nl", "https://github.com/Juraji")
                .show();
    }

    @FXML
    private void directoryTableContentClickAction(MouseEvent mouseEvent) {
        if (UIUtils.isDoublePrimaryClickEvent(mouseEvent)) {
            final Directory directory = getLastSelectedItem();

            if (directory != null) {
                Main.getPrimaryScene().pushContent(new EditDirectoryScene(directory));
            }
        }
    }

    @FXML
    private void menuEditContextOpenSourceAction(ActionEvent actionEvent) {
        final Directory directory = this.getLastSelectedItem();

        if (directory != null) {
            directory.desktopOpenSource();
        }
    }

    @FXML
    private void menuEditContextOpenTargetDirectoryAction(ActionEvent actionEvent) {
        final Directory directory = getLastSelectedItem();

        if (directory != null && directory.getTargetLocation() != null) {
            UIUtils.desktopOpen(directory.getTargetLocation());
        }
    }

    @FXML
    private Directory getLastSelectedItem() {
        final TableView.TableViewSelectionModel<Directory> selectionModel = directoryTable.getSelectionModel();
        final ObservableList<Integer> selectedIndices = selectionModel.getSelectedIndices();
        if (selectedIndices.size() > 1) {
            final int selectedIndex = selectedIndices.get(selectedIndices.size() - 1);
            selectionModel.clearAndSelect(selectedIndex);
        }

        return selectionModel.getSelectedItem();
    }

    @FXML
    private List<Directory> getSelectedItems() {
        return Collections.unmodifiableList(directoryTable.getSelectionModel().getSelectedItems());
    }
}
