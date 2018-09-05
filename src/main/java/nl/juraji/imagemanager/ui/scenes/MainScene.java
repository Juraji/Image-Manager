package nl.juraji.imagemanager.ui.scenes;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.ui.components.ETCText;
import nl.juraji.imagemanager.ui.util.BorderPaneScene;
import nl.juraji.imagemanager.ui.util.SceneConstructor;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class MainScene extends BorderPaneScene {

    private final LinkedList<Node> sceneHistoryStack = new LinkedList<>();
    private final Supplier<SceneConstructor> defaultContent;

    @FXML
    private Label statusBarDirectoryCountLabel;
    @FXML
    private Label statusBarTotalImageCountLabel;

    @FXML
    private HBox taskProgressContainer;
    @FXML
    private Label statusBarTaskProgressDescriptionLabel;
    @FXML
    private ETCText statusBarProgressETCLabel;
    @FXML
    private ProgressBar statusBarProgressBar;

    public MainScene(Supplier<SceneConstructor> defaultContent) {
        this.defaultContent = defaultContent;
        this.constructFXML();
        this.pushContent(defaultContent.get());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        this.updateStatusBar();
    }

    public void previousContent() {
        Node scene = sceneHistoryStack.pollLast();

        if (scene == null) {
            scene = defaultContent.get().getContentNode();
        }

        this.setCenter(scene);
    }

    public void pushContent(SceneConstructor sceneInstance) {
        this.pushContent(sceneInstance, false);
    }

    public void pushContent(SceneConstructor sceneInstance, boolean clearHistory) {
        final Node center = this.getCenter();

        if (clearHistory) {
            this.sceneHistoryStack.clear();
        } else {
            if (center != null) {
                this.sceneHistoryStack.add(center);
            }
        }

        this.setCenter(sceneInstance.getContentNode());
    }

    public void updateStatusBar() {
        final Dao dao = new Dao();
        final String dirs = TextUtils.format(resources,
                "MainScene.statusBar.DirectoryCount.label",
                dao.count(Directory.class));
        final String images = TextUtils.format(resources,
                "MainScene.statusBar.totalImageCount.label",
                dao.count(ImageMetaData.class));

        statusBarDirectoryCountLabel.setText(dirs);
        statusBarTotalImageCountLabel.setText(images);
    }

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
