package nl.juraji.imagemanager.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.NonInvertibleTransformException;
import nl.juraji.imagemanager.util.fxevents.MouseDragRecorder;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.listeners.ValueChangeListener;

/**
 * Created by Juraji on 2-9-2018.
 * Image Manager
 */
public class ImageViewer extends StackPane {
    private static final double MIN_ZOOM = 0.05;
    private static final double MAX_ZOOM = 30.0;
    private static final double SCROLL_ZOOM_FACTOR_UP = 1.05;
    private static final double SCROLL_ZOOM_FACTOR_DOWN = 0.95;

    private final SimpleDoubleProperty zoom;
    private final Pane imageRegion;
    private final ImageView imageView;

    public ImageViewer() {
        this.zoom = new SimpleDoubleProperty(1.0);

        this.imageRegion = new StackPane();
        getChildren().add(imageRegion);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCursor(Cursor.OPEN_HAND);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 3, 7);");

        imageView.setOnMousePressed(e -> imageView.setCursor(Cursor.CLOSED_HAND));
        imageView.setOnMouseReleased(e -> imageView.setCursor(Cursor.OPEN_HAND));

        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        imageRegion.getChildren().add(imageView);

        MouseDragRecorder mouseDragRecorder = new MouseDragRecorder(imageView);
        mouseDragRecorder.dragRecordProperty().addListener((ValueChangeListener<MouseDragRecorder.DragRecord>) d -> {
            imageRegion.setTranslateX(imageRegion.getTranslateX() + d.getDeltaX());
            imageRegion.setTranslateY(imageRegion.getTranslateY() + d.getDeltaY());
        });

        addEventHandler(ScrollEvent.SCROLL, this::scrollEventHandler);
        addEventHandler(ZoomEvent.ZOOM, this::zoomEventHandler);
    }

    public void zoom(double zoomFactor, Point2D pointOnImage) {
        final double scaleX = imageRegion.getScaleX();
        final double scaleY = imageRegion.getScaleY();

        if ((zoomFactor < 1 && scaleX <= MIN_ZOOM) || (zoomFactor > 1 && scaleX >= MAX_ZOOM)) {
            return;
        }

        double currentX = pointOnImage.getX();
        double currentY = pointOnImage.getY();

        double currentDistanceFromCenterX = currentX - imageRegion.getBoundsInLocal().getWidth() / 2;
        double currentDistanceFromCenterY = currentY - imageRegion.getBoundsInLocal().getHeight() / 2;

        double addScaleX = currentDistanceFromCenterX * zoomFactor;
        double addScaleY = currentDistanceFromCenterY * zoomFactor;

        double translationX = addScaleX - currentDistanceFromCenterX;
        double translationY = addScaleY - currentDistanceFromCenterY;

        imageRegion.setTranslateX(imageRegion.getTranslateX() - translationX * scaleX);
        imageRegion.setTranslateY(imageRegion.getTranslateY() - translationY * scaleY);

        imageRegion.setScaleX(scaleX * zoomFactor);
        imageRegion.setScaleY(scaleY * zoomFactor);

        zoom.setValue(scaleX);
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

    public void setImage(Image image) {
        imageView.setImage(image);

        if (image != null) {
            imageRegion.setScaleX(1);
            imageRegion.setScaleY(1);

            final Parent parent = getParent();

            if (parent instanceof Pane) {
                Pane parentPane = (Pane) parent;

                final double xPadding = parentPane.getPadding().getLeft();
                imageRegion.setTranslateX((parentPane.getWidth() - image.getWidth()) / 2 + xPadding);

                final double yPadding = parentPane.getPadding().getTop();
                imageRegion.setTranslateY((parentPane.getHeight() - image.getHeight()) / 2 + yPadding);
            }
        }
    }

    private void scrollEventHandler(ScrollEvent e) {
        try {
            final Point2D pointOnImage = UIUtils.pointInSceneFor(imageRegion, e.getSceneX(), e.getSceneY());
            final double zoomFactor = e.getDeltaY() > 0 ? SCROLL_ZOOM_FACTOR_UP : SCROLL_ZOOM_FACTOR_DOWN;
            zoom(zoomFactor, pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }

    private void zoomEventHandler(ZoomEvent e) {
        try {
            Point2D pointOnImage = UIUtils.pointInSceneFor(imageRegion, e.getSceneX(), e.getSceneY());
            zoom(e.getZoomFactor(), pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }
}
