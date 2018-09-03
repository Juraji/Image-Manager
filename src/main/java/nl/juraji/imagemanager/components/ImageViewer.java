package nl.juraji.imagemanager.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
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
    private static final double MIN_ZOOM = 0.01;
    private static final double MAX_ZOOM = 50.0;
    private static final double INITIAL_ZOOM = 1.0;
    private static final double ZOOM_PADDING = 40;
    private static final double SCROLL_ZOOM_FACTOR = 0.05;

    private final SimpleDoubleProperty zoom;
    private final SimpleDoubleProperty minZoom;
    private final SimpleDoubleProperty maxZoom;
    private final SimpleDoubleProperty zoomPadding;
    private final SimpleDoubleProperty scrollZoomFactor;

    private final Pane imageRegion;
    private final ImageView imageView;

    public ImageViewer() {
        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        minZoom = new SimpleDoubleProperty(MIN_ZOOM);
        maxZoom = new SimpleDoubleProperty(MAX_ZOOM);
        zoomPadding = new SimpleDoubleProperty(ZOOM_PADDING);
        scrollZoomFactor = new SimpleDoubleProperty(SCROLL_ZOOM_FACTOR);

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

        minZoom.addListener((ValueChangeListener<Number>) newValue -> {
            if (newValue.doubleValue() > zoom.get()) {
                this.resetZoomAndPosition();
            }
        });
        maxZoom.addListener((ValueChangeListener<Number>) newValue -> {
            if (newValue.doubleValue() < zoom.get()) {
                this.resetZoomAndPosition();
            }
        });
    }

    public double getZoom() {
        return zoom.get();
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

    public double getMinZoom() {
        return minZoom.get();
    }

    public SimpleDoubleProperty minZoomProperty() {
        return minZoom;
    }

    public double getMaxZoom() {
        return maxZoom.get();
    }

    public SimpleDoubleProperty maxZoomProperty() {
        return maxZoom;
    }

    public double getZoomPadding() {
        return zoomPadding.get();
    }

    public SimpleDoubleProperty zoomPaddingProperty() {
        return zoomPadding;
    }

    public double getScrollZoomFactor() {
        return scrollZoomFactor.get();
    }

    public SimpleDoubleProperty scrollZoomFactorProperty() {
        return scrollZoomFactor;
    }

    public void setImage(Image image) {
        imageView.setImage(image);
        this.resetZoomAndPosition();
    }

    public void resetZoomAndPosition() {
        final Image image = imageView.getImage();
        if (image != null) {
            final double imageWidth = image.getWidth();
            final double imageHeight = image.getHeight();
            final Parent parent = getParent();

            // reset scale/zoom
            imageRegion.setScaleX(INITIAL_ZOOM);
            imageRegion.setScaleY(INITIAL_ZOOM);
            zoom.setValue(INITIAL_ZOOM);

            if (parent instanceof Pane) {
                final Pane parentPane = (Pane) parent;
                final double parentWidth = parentPane.getWidth();
                final double parentHeight = parentPane.getHeight();

                // Zoom to fit in parent pane
                final double paddedParentWidth = parentWidth - zoomPadding.get();
                final double paddedParentHeight = parentHeight - zoomPadding.get();
                if (imageWidth > imageHeight && imageWidth > paddedParentWidth) {
                    zoom(paddedParentWidth / imageWidth, new Point2D(0, 0));
                } else if (imageHeight > paddedParentHeight) {
                    zoom(paddedParentHeight / imageHeight, new Point2D(0, 0));
                }

                // Center image in parent pane
                final double xPadding = parentPane.getPadding().getLeft();
                imageRegion.setTranslateX((parentWidth - imageWidth) / 2 + xPadding);

                final double yPadding = parentPane.getPadding().getTop();
                imageRegion.setTranslateY((parentHeight - imageHeight) / 2 + yPadding);
            }
        }
    }

    public void zoom(double zoomFactor, Point2D pointOnImage) {
        final double scaleX = imageRegion.getScaleX();
        final double scaleY = imageRegion.getScaleY();

        if ((zoomFactor < 1 && scaleX <= minZoom.get()) || (zoomFactor > 1 && scaleX >= maxZoom.get())) {
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

        zoom.setValue(imageRegion.getScaleX());
    }

    private void scrollEventHandler(ScrollEvent e) {
        try {
            final Point2D pointOnImage = UIUtils.pointInSceneFor(imageRegion, e.getSceneX(), e.getSceneY());
            final double zoomFactor = 1.0 + (e.getDeltaY() > 0 ? scrollZoomFactor.get() : -scrollZoomFactor.get());
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
