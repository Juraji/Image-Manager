package nl.juraji.imagemanager.util.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Window;
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

    public static ResourceBundle getI18nBundle() {
        return ResourceBundle.getBundle(I18N_RESOURCE_BUNDLE_BASE, Preferences.getLocale());
    }

    public static Node createView(Class<?> controllerClass, Object data) {
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
            loader.setResources(getI18nBundle());
            final Node fxmlView = loader.load();

            // If controller implements InitializableWithData and data not is null call initializeWithData
            if (InitializableWithData.class.isAssignableFrom(controllerClass)) {
                InitializableWithData<Object> controller = loader.getController();
                controller.initializeWithData(loader.getLocation(), loader.getResources(), data);
            }

            // Node created, return result
            return fxmlView;
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

    public static void centerOn(Window self, Window owner) {
        if (!self.isShowing()) {
            throw new IllegalArgumentException("Self should be rendered before calling #centerOn, self: " + self.getClass().getName());
        }
        final double selfXCenterPoint = self.getWidth() / 2;
        final double selfYCenterPoint = self.getHeight() / 2;
        final double ownerXCenterPoint = owner.getWidth() / 2;
        final double ownerYCenterPoint = owner.getHeight() / 2;
        self.setX(owner.getX() + (ownerXCenterPoint - selfXCenterPoint));
        self.setY(owner.getY() + (ownerYCenterPoint - selfYCenterPoint));
    }
}
