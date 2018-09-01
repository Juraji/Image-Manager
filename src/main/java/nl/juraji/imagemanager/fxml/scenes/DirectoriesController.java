package nl.juraji.imagemanager.fxml.scenes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.tasks.*;
import nl.juraji.imagemanager.util.Preferences;
import nl.juraji.imagemanager.util.concurrent.TaskQueueBuilder;
import nl.juraji.imagemanager.util.ui.*;

import javax.security.auth.login.CredentialException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public class DirectoriesController implements Initializable {

    private final ObservableList<Directory> directoryTableModel = FXCollections.observableArrayList();
    private final Dao dao = new Dao();

    private ResourceBundle resources;

    public TableView<Directory> directoryTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

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

    public void menuFileSettingsAction(ActionEvent actionEvent) {
        Main.switchToScene(SettingsController.class);
    }

    public void menuFileExitApplicationAction(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void menuAddAddDirectoryAction(ActionEvent e) {
        DirectoryChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("directoriesController.menuAddDirectoryAction.directoryChooser.title"))
                .show(f -> {
                    final Directory directory = new Directory();
                    directory.setName(f.getName());
                    directory.setTargetLocation(f);

                    dao.save(directory);
                    directoryTableModel.add(directory);

                    ToastBuilder.create(Main.getPrimaryStage())
                            .withMessage(resources.getString("directoriesController.menuAddDirectoryAction.toast"), f.getAbsolutePath())
                            .show();

                    directoryTable.getSelectionModel().clearSelection();
                    directoryTable.getSelectionModel().select(directory);
                    Main.getPrimaryController().updateStatusBar();
                });
    }

    public void menuAddAddPinterestBoardsAction(ActionEvent actionEvent) {
        Consumer<List<PinterestBoard>> selectedBoardsHandler = result -> PinterestBoardChooserBuilder.create(Main.getPrimaryStage())
                .withTitle(resources.getString("directoriesController.menuAddAddPinterestBoardsAction.selectBoards.title"))
                .withPinterestBoards(result)
                .showAndWait()
                .ifPresent(selected -> {
                    new Dao().save(selected);

                    directoryTableModel.addAll(selected);
                    directoryTable.getSelectionModel().clearSelection();
                    selected.forEach(board -> directoryTable.getSelectionModel().select(board));
                    Main.getPrimaryController().updateStatusBar();
                });

        Consumer<Throwable> exceptionHandler = e -> {
            ToastBuilder.create(Main.getPrimaryStage())
                    .withMessage("An error occurred while fetching Pinterest boards, try again later.\nError details: " + e.getMessage())
                    .show();
        };

        try {
            TaskQueueBuilder.create(resources)
                    .appendTask(new FindPinterestBoardsTask(), selectedBoardsHandler, exceptionHandler)
                    .run();
        } catch (CredentialException e) {
            AlertBuilder.createWarning()
                    .withTitle("No login set for Pinterest service")
                    .withContext("You haven't yet set any pinterest authentication information.\nDo so by going to File -> Settings and fill out the form under Pinterest Settings.")
                    .show();
        }

    }

    public void menuEditRefreshImageMetaDataAction(ActionEvent actionEvent) {
        final List<Directory> directories = getSelectedItems();

        if (directories.size() > 0) {
            final TaskQueueBuilder queueBuilder = TaskQueueBuilder.create(resources);

            ToastBuilder.create(Main.getPrimaryStage())
                    .withMessage(resources.getString("directoriesController.refreshMetaDataAction.running.toast"), directories.size())
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
                    .onSucceeded(() -> ToastBuilder.create(Main.getPrimaryStage())
                            .withMessage(resources.getString("directoriesController.refreshMetaDataAction.completed.toast"), directories.size())
                            .show())
                    .onSucceeded(() -> Main.getPrimaryController().updateStatusBar())
                    .run();
        }
    }

    public void menuEditDirectoryAction(ActionEvent actionEvent) {
        final Directory item = getLastSelectedItem();

        if (item != null) {
            Main.switchToScene(EditDirectoryController.class, item);
        }
    }

    public void menuEditDeleteDirectoriesAction(ActionEvent actionEvent) {
        final List<Directory> items = getSelectedItems();
        if (items.size() > 0) {
            final int itemCount = items.size();
            AlertBuilder.createConfirm()
                    .withTitle(resources.getString("directoriesController.deleteDirectoriesAction.warning.title"), itemCount)
                    .withContext(resources.getString("directoriesController.deleteDirectoriesAction.warning.context"))
                    .show(() -> {
                        dao.delete(items);

                        ToastBuilder.create(Main.getPrimaryStage())
                                .withMessage(resources.getString("directoriesController.deleteDirectoriesAction.toast"), itemCount)
                                .show();

                        Main.getPrimaryController().updateStatusBar();
                        Main.switchToScene(DirectoriesController.class);
                    });
        }
    }

    public void menuScannersDuplicateScannerAction(ActionEvent actionEvent) {
        Main.switchToScene(DuplicateScansController.class);
    }

    public void menuHelpAboutAction(ActionEvent actionEvent) {
        AlertBuilder.createInfo()
                .withTitle(resources.getString("directoriesController.menuAboutAction.title"))
                .withContext("Image Manager 1.0.0\n© Juraji {}\n{}\nGithub: {}",
                        LocalDate.now().getYear(), "https://juraji.nl", "https://github.com/Juraji")
                .show();
    }

    public void directoryTableContentClickAction(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                && mouseEvent.getClickCount() == 2) {
            final Directory directory = getLastSelectedItem();

            if (directory != null) {
                Main.switchToScene(EditDirectoryController.class, directory);
            }
        }
    }

    public void menuEditContextOpenSourceAction(ActionEvent actionEvent) {
        final Directory directory = this.getLastSelectedItem();

        if (directory != null) {
            directory.desktopOpenSource();
        }
    }

    public void menuEditContextOpenTargetDirectoryAction(ActionEvent actionEvent) {
        final Directory directory = getLastSelectedItem();

        if (directory != null && directory.getTargetLocation() != null) {
            UIUtils.desktopOpen(directory.getTargetLocation());
        }
    }

    private Directory getLastSelectedItem() {
        final TableView.TableViewSelectionModel<Directory> selectionModel = directoryTable.getSelectionModel();
        final ObservableList<Integer> selectedIndices = selectionModel.getSelectedIndices();
        if (selectedIndices.size() > 1) {
            final int selectedIndex = selectedIndices.get(selectedIndices.size() - 1);
            selectionModel.clearAndSelect(selectedIndex);
        }

        return selectionModel.getSelectedItem();
    }

    private List<Directory> getSelectedItems() {
        return Collections.unmodifiableList(directoryTable.getSelectionModel().getSelectedItems());
    }
}
