package nl.juraji.imagemanager.ui.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import nl.juraji.imagemanager.ui.util.FXMLConstructor;
import nl.juraji.imagemanager.util.fxevents.MouseDragRecorder;
import nl.juraji.imagemanager.util.math.Rotation;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.events.DefaultMouseEvent;
import nl.juraji.imagemanager.util.ui.events.ValueChangeListener;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 3-9-2018.
 * Image Manager
 */
public class ImageViewer extends AnchorPane implements FXMLConstructor, Initializable {

    private static final double INITIAL_ZOOM = 1.0;
    private static final double ZOOM_PADDING = 20;
    private static final double SCROLL_ZOOM_FACTOR = 0.05;

    private final SimpleDoubleProperty zoom;
    private final SimpleDoubleProperty zoomPadding;
    private final SimpleDoubleProperty scrollZoomFactor;

    @FXML
    private ImageView imageView;
    @FXML
    private Label zoomLabel;

    public ImageViewer() {
        this.zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        this.zoomPadding = new SimpleDoubleProperty(ZOOM_PADDING);
        this.scrollZoomFactor = new SimpleDoubleProperty(SCROLL_ZOOM_FACTOR);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.clipChildren(this);

        this.imageView.setOnMousePressed(e -> this.imageView.setCursor(Cursor.CLOSED_HAND));
        this.imageView.setOnMouseReleased(e -> this.imageView.setCursor(Cursor.OPEN_HAND));

        this.zoomLabel.textProperty().bind(zoom
                .multiply(100)
                .asString(resources.getString("ImageViewer.statusBarZoomLevel.label")));

        MouseDragRecorder mouseDragRecorder = new MouseDragRecorder(imageView);
        mouseDragRecorder.dragRecordProperty().addListener((ValueChangeListener<MouseDragRecorder.DragRecord>) d -> {
            imageView.setTranslateX(imageView.getTranslateX() + d.getDeltaX());
            imageView.setTranslateY(imageView.getTranslateY() + d.getDeltaY());
        });
    }

    public double getZoom() {
        return zoom.get();
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
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

    public double getPaddedWidth() {
        return this.getWidth() - this.zoomPadding.get();
    }

    public double getPaddedHeight() {
        return this.getHeight() - this.zoomPadding.get();
    }

    public double getImageWidth() {
        return this.imageView.getImage().getWidth();
    }

    public double getImageHeight() {
        return this.imageView.getImage().getHeight();
    }

    public void resetZoomAndPosition() {
        if (imageView.getImage() != null) {
            // Zoom to fit in parent pane (if necessary)
            if (getImageWidth() > getPaddedWidth() || getImageHeight() > getPaddedHeight()) {
                zoomToFit(new DefaultMouseEvent());
            } else {
                // reset scale/zoom
                zoomToOriginalSize(new DefaultMouseEvent());
            }

            rotate(0.0);
        }
    }

    public void centerImage() {
        imageView.setTranslateX(-((getImageWidth() - getWidth()) / 2));
        imageView.setTranslateY(-((getImageHeight() - getHeight()) / 2));
    }

    public void zoomToOriginalSize(MouseEvent event) {
        event.consume();
        centerImage();

        imageView.setScaleX(INITIAL_ZOOM);
        imageView.setScaleY(INITIAL_ZOOM);
        zoom.setValue(INITIAL_ZOOM);
    }

    public void zoomToFit(MouseEvent event) {
        event.consume();
        zoomToOriginalSize(event);
        final double xZoom = getPaddedWidth() / getImageWidth();
        final double yZoom = getPaddedHeight() / getImageHeight();

        if (xZoom < yZoom) {
            zoom(xZoom, null);
        } else {
            zoom(yZoom, null);
        }
    }

    public void rotateClockwise90(MouseEvent event) {
        event.consume();
        final double imageRot = Rotation.rotate(imageView.getRotate(), Rotation.QUARTER_CIRCLE);
        this.rotate(imageRot);
    }

    public void rotateCounterclockwise90(MouseEvent event) {
        event.consume();
        final double imageRot = Rotation.rotate(imageView.getRotate(), Rotation.invert(Rotation.QUARTER_CIRCLE));
        this.rotate(imageRot);
    }

    public void rotate(double deg) {
        final double[] offsets = Rotation.rotateCoordinates(deg, 3, 7);
        final DropShadow dropShadow = new DropShadow(10, offsets[0], offsets[1],
                new Color(0, 0, 0, 0.5));

        imageView.setRotate(deg);
        imageView.setEffect(dropShadow);
    }

    public void zoom(double zoomFactor, Point2D pointOnImage) {
        final double scaleX = imageView.getScaleX();
        final double scaleY = imageView.getScaleY();

        if (pointOnImage != null) {
            double currentX = pointOnImage.getX();
            double currentY = pointOnImage.getY();

            double currentDistanceFromCenterX = currentX - imageView.getBoundsInLocal().getWidth() / 2;
            double currentDistanceFromCenterY = currentY - imageView.getBoundsInLocal().getHeight() / 2;

            double addScaleX = currentDistanceFromCenterX * zoomFactor;
            double addScaleY = currentDistanceFromCenterY * zoomFactor;

            double translationX = addScaleX - currentDistanceFromCenterX;
            double translationY = addScaleY - currentDistanceFromCenterY;

            imageView.setTranslateX(imageView.getTranslateX() - translationX * scaleX);
            imageView.setTranslateY(imageView.getTranslateY() - translationY * scaleY);
        }

        imageView.setScaleX(scaleX * zoomFactor);
        imageView.setScaleY(scaleY * zoomFactor);

        zoom.setValue(imageView.getScaleX());
    }

    @FXML
    private void onScrollEvent(ScrollEvent e) {
        try {
            final Point2D pointOnImage = UIUtils.pointInSceneFor(imageView, e.getSceneX(), e.getSceneY());
            final double zoomFactor = 1.0 + (e.getDeltaY() > 0 ? scrollZoomFactor.get() : -scrollZoomFactor.get());
            zoom(zoomFactor, pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    private void onZoomEvent(ZoomEvent e) {
        try {
            Point2D pointOnImage = UIUtils.pointInSceneFor(imageView, e.getSceneX(), e.getSceneY());
            zoom(e.getZoomFactor(), pointOnImage);
        } catch (NonInvertibleTransformException e1) {
            e1.printStackTrace();
        }
    }
}