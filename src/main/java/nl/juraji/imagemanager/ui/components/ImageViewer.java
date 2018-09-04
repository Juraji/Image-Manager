package nl.juraji.imagemanager.ui.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.NonInvertibleTransformException;
import nl.juraji.imagemanager.ui.util.FXMLConstructor;
import nl.juraji.imagemanager.util.fxevents.MouseDragRecorder;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.listeners.ValueChangeListener;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class ImageViewer extends StackPane implements FXMLConstructor, Initializable {

    private static final double MIN_ZOOM = 0.01;
    private static final double MAX_ZOOM = 50.0;
    private static final double INITIAL_ZOOM = 1.0;
    private static final double ZOOM_PADDING = 80;
    private static final double SCROLL_ZOOM_FACTOR = 0.05;

    private final SimpleDoubleProperty zoom;
    private final SimpleDoubleProperty minZoom;
    private final SimpleDoubleProperty maxZoom;
    private final SimpleDoubleProperty zoomPadding;
    private final SimpleDoubleProperty scrollZoomFactor;

    @FXML
    private StackPane imageRegion;
    @FXML
    private ImageView imageView;
    @FXML
    private Label zoomLabel;

    public ImageViewer() {
        this.zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        this.minZoom = new SimpleDoubleProperty(MIN_ZOOM);
        this.maxZoom = new SimpleDoubleProperty(MAX_ZOOM);
        this.zoomPadding = new SimpleDoubleProperty(ZOOM_PADDING);
        this.scrollZoomFactor = new SimpleDoubleProperty(SCROLL_ZOOM_FACTOR);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.imageView.setOnMousePressed(e -> this.imageView.setCursor(Cursor.CLOSED_HAND));
        this.imageView.setOnMouseReleased(e -> this.imageView.setCursor(Cursor.OPEN_HAND));

        this.zoomLabel.textProperty().bind(zoom
                .multiply(100)
                .asString(resources.getString("editImageController.statusBarZoomLevel.label")));

        MouseDragRecorder mouseDragRecorder = new MouseDragRecorder(imageView);
        mouseDragRecorder.dragRecordProperty().addListener((ValueChangeListener<MouseDragRecorder.DragRecord>) d -> {
            imageRegion.setTranslateX(imageRegion.getTranslateX() + d.getDeltaX());
            imageRegion.setTranslateY(imageRegion.getTranslateY() + d.getDeltaY());
        });

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

    public void setMinZoom(double minZoom) {
        this.minZoom.set(minZoom);
    }

    public double getMaxZoom() {
        return maxZoom.get();
    }

    public SimpleDoubleProperty maxZoomProperty() {
        return maxZoom;
    }

    public void setMaxZoom(double maxZoom) {
        this.maxZoom.set(maxZoom);
    }

    public double getZoomPadding() {
        return zoomPadding.get();
    }

    public SimpleDoubleProperty zoomPaddingProperty() {
        return zoomPadding;
    }

    public void setZoomPadding(double zoomPadding) {
        this.zoomPadding.set(zoomPadding);
    }

    public double getScrollZoomFactor() {
        return scrollZoomFactor.get();
    }

    public SimpleDoubleProperty scrollZoomFactorProperty() {
        return scrollZoomFactor;
    }

    public void setScrollZoomFactor(double scrollZoomFactor) {
        this.scrollZoomFactor.set(scrollZoomFactor);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
        this.resetZoomAndPosition();
    }

    public Image getImage() {
        return imageView.getImage();
    }

    public void resetZoomAndPosition() {
        final Image image = imageView.getImage();

        if (image != null) {
            final double imageWidth = image.getWidth();
            final double imageHeight = image.getHeight();
            final Pane parentPane = getParentPane();
            final double zoomPadding = this.zoomPadding.get();

            // reset scale/zoom
            imageRegion.setScaleX(INITIAL_ZOOM);
            imageRegion.setScaleY(INITIAL_ZOOM);
            zoom.setValue(INITIAL_ZOOM);

            final double parentWidth = parentPane.getWidth();
            final double parentHeight = parentPane.getHeight();

            // Zoom to fit in parent pane
            final double paddedParentWidth = parentWidth - zoomPadding;
            final double paddedParentHeight = parentHeight - zoomPadding;
            if (imageWidth > imageHeight && imageWidth > paddedParentWidth) {
                imageRegion.setTranslateX(-((imageWidth - parentWidth) / 2));
                zoom(paddedParentWidth / imageWidth, null);
            } else if (imageHeight > paddedParentHeight) {
                imageRegion.setTranslateY(-((imageHeight - parentHeight) / 2));
                zoom(paddedParentHeight / imageHeight, null);
            }
        }
    }

    public void zoom(double zoomFactor, Point2D pointOnImage) {
        final double scaleX = imageRegion.getScaleX();
        final double scaleY = imageRegion.getScaleY();

        if ((zoomFactor < 1 && scaleX <= minZoom.get()) || (zoomFactor > 1 && scaleX >= maxZoom.get())) {
            return;
        }

        if (pointOnImage != null) {
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
        }

        imageRegion.setScaleX(scaleX * zoomFactor);
        imageRegion.setScaleY(scaleY * zoomFactor);

        zoom.setValue(imageRegion.getScaleX());
    }

    public void onScrollEvent(ScrollEvent e) {
        try {
            final Point2D pointOnImage = UIUtils.pointInSceneFor(imageRegion, e.getSceneX(), e.getSceneY());
            final double zoomFactor = 1.0 + (e.getDeltaY() > 0 ? scrollZoomFactor.get() : -scrollZoomFactor.get());
            zoom(zoomFactor, pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }

    public void onZoomEvent(ZoomEvent e) {
        try {
            Point2D pointOnImage = UIUtils.pointInSceneFor(imageRegion, e.getSceneX(), e.getSceneY());
            zoom(e.getZoomFactor(), pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }

    private Pane getParentPane() {
        return (Pane) getParent();
    }
}
