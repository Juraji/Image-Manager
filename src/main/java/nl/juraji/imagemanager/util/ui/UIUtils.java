package nl.juraji.imagemanager.util.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import nl.juraji.imagemanager.util.fxevents.ValueChangeListener;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean isDoublePrimaryClickEvent(MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2;
    }

    public static Stage getStage(ActionEvent actionEvent) {
        return getStage((Node) actionEvent.getSource());
    }

    public static Stage getStage(InputEvent actionEvent) {
        return getStage((Node) actionEvent.getSource());
    }

    public static Stage getStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    public static String formatDateTime(LocalDateTime dateTime, FormatStyle style) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(style);
        return dateTime.atZone(ZoneId.systemDefault()).format(formatter);
    }

    public static Point2D pointInSceneFor(Node node, double sceneX, double sceneY) throws NonInvertibleTransformException {
        return node.getLocalToSceneTransform().inverseTransform(sceneX, sceneY);
    }

    /**
     * Clips the children of the specified {@link Region} to its current size.
     * This requires attaching a change listener to the region’s layout bounds,
     * as JavaFX does not currently provide any built-in way to clip children.
     *
     * @param region the {@link Region} whose children to clip
     * @throws NullPointerException if {@code region} is {@code null}
     */
    public static void clipChildren(Region region) {

        final Rectangle outputClip = new Rectangle();
        region.setClip(outputClip);

        region.layoutBoundsProperty().addListener((ValueChangeListener<Bounds>) newValue -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }
}
