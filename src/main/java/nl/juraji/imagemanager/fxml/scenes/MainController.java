package nl.juraji.imagemanager.fxml.scenes;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import nl.juraji.imagemanager.components.ETCText;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 1-9-2018.
 * Image Manager
 */
public class MainController implements Initializable {
    private ResourceBundle resources;

    public BorderPane container;
    public Label statusBarDirectoryCountLabel;
    public Label statusBarTotalImageCountLabel;

    public HBox taskProgressContainer;
    public Label statusBarTaskProgressDescriptionLabel;
    public ETCText statusBarProgressETCLabel;
    public ProgressBar statusBarProgressBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.updateStatusBar();
    }

    /**
     * Set new window content
     *
     * @param content The content node to set
     */
    public void setContent(Node content) {
        this.container.setCenter(content);
    }

    /**
     * Update the status bar labels
     */
    public void updateStatusBar() {
        final Dao dao = new Dao();

        final long directoryCount = dao.count(Directory.class);
        final long imageCount = dao.count(ImageMetaData.class);

        final String dirs = TextUtils.format(resources, "mainController.statusBar.DirectoryCount.label", directoryCount);
        final String images = TextUtils.format(resources, "mainController.statusBar.totalImageCount.label", imageCount);

        statusBarDirectoryCountLabel.setText(dirs);
        statusBarTotalImageCountLabel.setText(images);
    }

    /**
     * Bind a task to the progressbar
     *
     * @param task        The task to bind
     */
    public void activateProgressBar(final QueueTask task) {
        if (task == null || task.isDone()) {
            // Do not show when task is already done
            return;
        }

        // Set description
        statusBarTaskProgressDescriptionLabel.setText(task.getTaskTitle(resources));

        // Unbind any current bindings and bind new task
        statusBarProgressBar.progressProperty().unbind();
        statusBarProgressBar.setProgress(-1);
        statusBarProgressBar.progressProperty().bind(task.progressProperty());

        statusBarProgressETCLabel.progressProperty().unbind();
        statusBarProgressETCLabel.setProgress(-1);
        statusBarProgressETCLabel.progressProperty().bind(task.progressProperty());

        task.runningProperty().addListener((o, wasRunning, isRunning) -> taskProgressContainer.setVisible(isRunning));
    }
}
