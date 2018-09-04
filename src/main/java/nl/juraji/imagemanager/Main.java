package nl.juraji.imagemanager;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.ui.scenes.DirectoriesScene;
import nl.juraji.imagemanager.ui.scenes.MainScene;
import nl.juraji.imagemanager.ui.util.SceneConstructor;
import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.io.web.drivers.WebDriverPool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class Main extends Application {
    private static final AtomicReference<Stage> PRIMARY_STAGE = new AtomicReference<>();
    private static final AtomicReference<MainScene> PRIMARY_SCENE = new AtomicReference<>();

    public static void main(String[] args) {
        handleArgs(args);

        // Launch application
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return PRIMARY_STAGE.get();
    }

    public static void previousScene() {
        PRIMARY_SCENE.get().previousContent();
    }

    public static void switchToScene(SceneConstructor sceneInstance) {
        PRIMARY_SCENE.get().pushContent(sceneInstance);
    }

    public static void switchToScene(SceneConstructor sceneInstance, boolean replaceHistory) {
        PRIMARY_SCENE.get().pushContent(sceneInstance, replaceHistory);
    }

    public static MainScene getPrimaryScene() {
        return PRIMARY_SCENE.get();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            final MainScene mainScene = new MainScene();

            primaryStage.setTitle("Image Manager");
            primaryStage.setScene(mainScene.createScene());
            primaryStage.getIcons().add(new Image(Main.class
                    .getResourceAsStream("/nl/juraji/imagemanager/images/application.png")));
            primaryStage.show();

            PRIMARY_STAGE.set(primaryStage);
            PRIMARY_SCENE.set(mainScene);

            switchToScene(new DirectoriesScene());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        // Shutdown Dao
        Dao.shutDown();
        WebDriverPool.shutdown();
        System.exit(0);
    }

    private static void handleArgs(String[] args) {
        final HashMap<String, Runnable> argHandlers = new HashMap<>();
        argHandlers.put("--log-debug", Log::enableRootLogDebug);

        Arrays.stream(args).forEach(arg -> {
            if (argHandlers.containsKey(arg)) {
                argHandlers.get(arg).run();
            }
        });
    }
}
