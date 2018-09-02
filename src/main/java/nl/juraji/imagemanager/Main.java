package nl.juraji.imagemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.juraji.imagemanager.fxml.scenes.DirectoriesController;
import nl.juraji.imagemanager.fxml.scenes.MainController;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.io.web.drivers.WebDriverPool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static nl.juraji.imagemanager.util.ui.FXML.createLoader;
import static nl.juraji.imagemanager.util.ui.FXML.createView;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class Main extends Application {
    private static final AtomicReference<Stage> PRIMARY_STAGE = new AtomicReference<>();
    private static final AtomicReference<MainController> PRIMARY_SCENE_CONTROLLER = new AtomicReference<>();

    public static void main(String[] args) {
        handleArgs(args);

        // Launch application
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return PRIMARY_STAGE.get();
    }

    public static void switchToScene(Class<?> controllerClass) {
        switchToScene(controllerClass, null);
    }

    public static void switchToScene(Class<?> controllerClass, Object data) {
        PRIMARY_SCENE_CONTROLLER.get().setContent(createView(controllerClass, data));
    }

    public static MainController getPrimaryController() {
        return PRIMARY_SCENE_CONTROLLER.get();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final FXMLLoader loader = createLoader(MainController.class);
            final Parent parent = loader.load();
            final MainController primaryController = loader.getController();
            final Scene primaryScene = new Scene(parent);

            primaryStage.setTitle("Image Manager");
            primaryStage.setScene(primaryScene);
            primaryStage.getIcons().add(new Image(Main.class
                    .getResourceAsStream("/nl/juraji/imagemanager/images/application.png")));
            primaryStage.show();

            PRIMARY_STAGE.set(primaryStage);
            PRIMARY_SCENE_CONTROLLER.set(primaryController);

            switchToScene(DirectoriesController.class);
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
