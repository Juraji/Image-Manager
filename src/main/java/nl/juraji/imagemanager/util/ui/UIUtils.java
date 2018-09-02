package nl.juraji.imagemanager.util.ui;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.juraji.imageio.webp.support.javafx.WebPJavaFX;
import nl.juraji.imagemanager.util.io.FileInputStream;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class UIUtils {
    private UIUtils() {
    }

    /**
     * Open a file on the local system
     *
     * @param location The file to open
     */
    public static void desktopOpen(File location) {
        if (location != null && location.exists()) {
            try {
                Desktop.getDesktop().open(location);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Open a (web) uri in the system browser
     *
     * @param location The (web) uri to open
     */
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

    public static Image safeLoadImage(File file) {
        return safeLoadImage(file, -1, -1);
    }

    public static Image safeLoadImage(File file, double preferredWidth, double preferredHeight) {
        try (FileInputStream stream = new FileInputStream(file)) {
            // Yay, WebP magic, since javafx does not support Image loader plugins
            if (WebPJavaFX.isWebPImage(file)) {
                return WebPJavaFX.createImageFromWebP(stream);
            } else {
                return new Image(stream, preferredWidth, preferredHeight, true, true);
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isDoublePrimaryClickEvent(MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2;
    }

    public static Stage getStage(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        return (Stage) source.getScene().getWindow();
    }
}
