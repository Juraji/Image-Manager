package nl.juraji.imagemanager.util.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import nl.juraji.imagemanager.util.Preferences;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

import static nl.juraji.imagemanager.util.ResourceUtils.I18N_RESOURCE_BUNDLE_BASE;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class UIUtils {
    private UIUtils() {
    }

    public static Scene createScene(Class<?> controllerClass, Object data) {
        try {
            // Check if controller class name ends with "Controller",
            // this is needed for inferring which fxml file to load
            if (!controllerClass.getSimpleName().endsWith("Controller")) {
                throw new RuntimeException("Controller classes should end with \"Controller\".");
            }

            // Create loader and infer FXML name
            final FXMLLoader loader = new FXMLLoader();
            final String fxmlFile = "/" + controllerClass.getName()
                    .replaceAll("\\.", "/")
                    .replaceAll("(.*)Controller$", "$1") + ".fxml";

            // Set loader location and load view
            loader.setLocation(UIUtils.class.getResource(fxmlFile));
            loader.setResources(ResourceBundle.getBundle(I18N_RESOURCE_BUNDLE_BASE, Preferences.getLocale()));
            final Parent scene = loader.load();

            // If controller implements InitializableWithData and data not is null call initializeWithData
            if (InitializableWithData.class.isAssignableFrom(controllerClass)) {
                InitializableWithData<Object> controller = loader.getController();
                controller.initializeWithData(loader.getLocation(), loader.getResources(), data);
            }

            // Scene created, return result
            return new Scene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void desktopOpen(File location) {
        if (location != null && location.exists()) {
            try {
                Desktop.getDesktop().open(location);
            } catch (IOException ignored) {
            }
        }
    }

    public static void desktopOpen(URI location) {
        if (location != null) {
            try {
                Desktop.getDesktop().browse(location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
