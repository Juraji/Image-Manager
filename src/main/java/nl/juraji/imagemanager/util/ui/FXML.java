package nl.juraji.imagemanager.util.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import nl.juraji.imagemanager.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public final class FXML {
    private FXML() {
    }

    public static URL getFXMLFor(Class<?> controllerClass) {
        final String fxmlFile = "/" + controllerClass.getName()
                .replaceAll("\\.", "/")
                .replaceAll("(.*)Controller$", "$1") + ".fxml";

        return UIUtils.class.getResource(fxmlFile);
    }

    public static FXMLLoader createLoader(Class<?> controllerClass) {

        // Create loader and infer FXML name
        final FXMLLoader loader = new FXMLLoader();

        // Set loader location and 18n resource bundle
        loader.setLocation(getFXMLFor(controllerClass));
        loader.setResources(ResourceUtils.getLocaleBundle());

        return loader;
    }

    /**
     * Initialize a FXML Controller and view
     *
     * @param controllerClass The -Controller class to load
     * @param data            Optional data (Required when controller implements {@link InitializableWithData})
     * @return The parent {@link Node} for the FXML view
     */
    public static Parent createView(Class<?> controllerClass, Object data) {
        try {
            // Check if controller class name ends with "Controller",
            // this is needed for inferring which fxml file to load
            if (!controllerClass.getSimpleName().endsWith("Controller")) {
                throw new RuntimeException("Controller classes should end with \"Controller\".");
            }

            // Create loader and load view
            final FXMLLoader loader = createLoader(controllerClass);
            final Parent fxmlView = loader.load();

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
}
