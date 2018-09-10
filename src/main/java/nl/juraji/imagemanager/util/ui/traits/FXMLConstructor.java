package nl.juraji.imagemanager.util.ui.traits;

import javafx.fxml.FXMLLoader;
import nl.juraji.imagemanager.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public interface FXMLConstructor {

    /**
     * Internal function
     * Use in class constructor to load accompanying FXML file
     */
    default void constructFXML() {
        Class<?> controllerClass = getClass();
        String fxmlResourceName;
        URL fxmlUrl;

        // Get controller FXML file
        // Keep going up the class structure, if the current class does not have an FXML file
        // until there's no more super class. This is to support inheritance of controllers, without copying the FXML.
        do {
            fxmlResourceName = "/" + controllerClass.getName().replace(".", "/") + ".fxml";
            fxmlUrl = FXMLConstructor.class.getResource(fxmlResourceName);
            controllerClass = controllerClass.getSuperclass();
        } while (fxmlUrl == null && controllerClass != null);

        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML file not found for class " + getClass().getName());
        }

        final FXMLLoader loader = new FXMLLoader();
        loader.setRoot(this);
        loader.setLocation(fxmlUrl);
        loader.setControllerFactory(param -> this);
        loader.setResources(ResourceUtils.getLocaleBundle());

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
