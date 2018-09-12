package nl.juraji.imagemanager.ui.scenes;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.ui.components.ETCText;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import nl.juraji.imagemanager.util.concurrent.ProcessExecutor;
import nl.juraji.imagemanager.util.fxevents.AcceleratorMap;
import nl.juraji.imagemanager.util.ui.traits.BorderPaneScene;
import nl.juraji.imagemanager.util.ui.traits.SceneConstructor;

import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class MainScene extends BorderPaneScene {

    private final LinkedList<SceneConstructor> sceneHistoryStack = new LinkedList<>();
    private final AtomicObject<SceneConstructor> currentScene = new AtomicObject<>();
    private final Supplier<SceneConstructor> defaultScene;
    private final ProcessExecutor processExecutor;

    @FXML
    private Label statusBarDirectoryCountLabel;
    @FXML
    private Label statusBarTotalImageCountLabel;
    @FXML
    private Label statusBarAcceleratorsLabel;

    @FXML
    private Label statusBarTaskQueueCountLabel;
    @FXML
    private HBox taskProgressContainer;
    @FXML
    private Label statusBarTaskProgressDescriptionLabel;
    @FXML
    private ETCText statusBarProgressETCLabel;
    @FXML
    private ProgressBar statusBarProgressBar;

    public MainScene(Supplier<SceneConstructor> defaultScene) {
        this.defaultScene = defaultScene;
        this.processExecutor = new ProcessExecutor();

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        this.taskProgressContainer.visibleProperty().bind(this.processExecutor.processRunningProperty());
        this.statusBarTaskProgressDescriptionLabel.textProperty().bind(this.processExecutor.processTitleProperty());
        this.statusBarProgressBar.progressProperty().bind(this.processExecutor.progressProperty());
        this.statusBarProgressETCLabel.progressProperty().bind(this.processExecutor.progressProperty());
        this.statusBarTaskQueueCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            final int queueCount = this.processExecutor.queueCountProperty().get();
            if (queueCount > 1) {
                return TextUtils.format(resources, "MainScene.statusBarTaskQueueCount.label", processExecutor.queueCountProperty().get());
            } else {
                return null;
            }
        }, processExecutor.queueCountProperty()));

        this.updateStatusBar();
    }

    @Override
    public void postInitialization() {
        this.pushContent(defaultScene.get());
    }

    public ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }

    public void previousContent() {
        SceneConstructor scene = sceneHistoryStack.pollLast();

        if (scene == null) {
            scene = defaultScene.get();
            this.pushContent(scene);
        } else {
            this.setContent(scene);
            scene.postReloadedInView();
        }

    }

    public void pushContent(SceneConstructor sceneInstance) {
        this.pushContent(sceneInstance, false);
    }

    public void pushContent(SceneConstructor scene, boolean clearHistory) {
        if (clearHistory) {
            this.sceneHistoryStack.clear();
            this.currentScene.clear();
        } else if (currentScene.isSet()) {
            final SceneConstructor current = currentScene.get();
            current.preUnloadedFromView();
            this.sceneHistoryStack.add(current);
        }

        this.setContent(scene);
        Platform.runLater(scene::postInitialization);
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

    private void setContent(SceneConstructor scene) {
        this.currentScene.set(scene);

        Platform.runLater(() -> {
            final Scene fxScene = getScene();

            // Update main fx scene accelerators with those from the content scene
            final AcceleratorMap accelerators = scene.getAccelerators();
            fxScene.getAccelerators().clear();
            fxScene.getAccelerators().putAll(accelerators);

            // Build a tooltip for the accelerator label in the status bar and update it
            if (accelerators.size() > 0) {
                final Tooltip acceleratorsTooltip = new Tooltip();
                final String acceleratorCombos = accelerators.createTooltipText(resources);

                acceleratorsTooltip.setText(acceleratorCombos);
                statusBarAcceleratorsLabel.setTooltip(acceleratorsTooltip);
            } else {
                statusBarAcceleratorsLabel.setTooltip(null);
            }

            // Update main fx scene mnemonics with those from the content scene
            fxScene.getMnemonics().clear();
            scene.getMnemonics().forEach(fxScene::addMnemonic);
        });

        this.setCenter(scene.getContentNode());
    }
}
