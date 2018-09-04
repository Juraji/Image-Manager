package nl.juraji.imagemanager.ui.util;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
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
        final String fxmlResourceName = "/" + getClass().getName().replace(".", "/") + ".fxml";
        final URL url = FXMLConstructor.class.getResource(fxmlResourceName);

        if (url == null) {
            throw new IllegalStateException("FXML file not found \"" + fxmlResourceName + "\" for class " + getClass().getName());
        }

        final FXMLLoader loader = new FXMLLoader();

        loader.setRoot(this);
        loader.setLocation(url);
        loader.setControllerFactory(param -> this);
        loader.setResources(ResourceUtils.getLocaleBundle());

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
