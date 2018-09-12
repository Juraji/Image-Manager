package nl.juraji.imagemanager.util.ui;

import javafx.scene.image.Image;
import nl.juraji.imageio.webp.support.javafx.WebPJavaFX;
import nl.juraji.imagemanager.util.io.FileInputStream;

import java.io.File;
import java.io.IOException;

/**
 * Created by Juraji on 12-9-2018.
 * Image Manager
 */
public final class ImageUtils {
    private ImageUtils() {
    }

    public static Image safeLoadImage(File file) {
        return safeLoadImage(file, -1, -1);
    }

    public static Image safeLoadImage(File file, double preferredWidth, double preferredHeight) {
        return safeLoadImage(file, preferredWidth, preferredHeight, true);
    }

    public static Image safeLoadImage(File file, double preferredWidth, double preferredHeight, boolean loadInBackground) {
        if (WebPJavaFX.isWebPImage(file)) {
            // Yay, WebP magic, since javafx does not support Image loader plugins
            try (FileInputStream stream = new FileInputStream(file)) {
                return WebPJavaFX.createImageFromWebP(stream);
            } catch (IOException ignored) {
            }
        } else {
            final String uri = file.toURI().toString();
            return new Image(uri, preferredWidth, preferredHeight, true, true, loadInBackground);
        }

        return null;
    }
}
