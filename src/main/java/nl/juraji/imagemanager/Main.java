package nl.juraji.imagemanager;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.fxml.scenes.DirectoriesController;
import nl.juraji.imagemanager.util.io.WebDriverPool;

import java.util.concurrent.atomic.AtomicReference;

import static nl.juraji.imagemanager.util.ui.UIUtils.createScene;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class Main extends Application {
    private static final AtomicReference<Stage> PRIMARY_STAGE = new AtomicReference<Stage>();

    public static void main(String[] args) {
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
        getPrimaryStage().setScene(createScene(controllerClass, data));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PRIMARY_STAGE.set(primaryStage);
        primaryStage.setTitle("Image Manager");
        primaryStage.setScene(createScene(DirectoriesController.class, null));
        primaryStage.getIcons().add(new Image(Main.class
                .getResourceAsStream("/nl/juraji/imagemanager/images/application.png")));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Shutdown Dao
        Dao.shutDown();
        WebDriverPool.shutdown();
    }
}
