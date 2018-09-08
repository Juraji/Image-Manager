package nl.juraji.imagemanager.ui.components;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import nl.juraji.imagemanager.util.fxevents.MouseDragRecorder;
import nl.juraji.imagemanager.util.math.Trigonometry2D;
import nl.juraji.imagemanager.util.ui.UIUtils;
import nl.juraji.imagemanager.util.ui.events.ValueChangeListener;
import nl.juraji.imagemanager.util.ui.traits.FXMLConstructor;

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
    private static final double DRAG_ROTATION_DIVISOR = 10.0;

    private final DoubleProperty zoom;
    private final DoubleProperty scrollZoomFactor;
    private final DoubleProperty imageWidth;
    private final DoubleProperty imageHeight;
    private final DoubleProperty imageRotation;
    private final ObjectProperty<Image> image;

    @FXML
    private ImageView imageView;
    @FXML
    private Label zoomLabel;

    public ImageViewer() {
        this.zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        this.scrollZoomFactor = new SimpleDoubleProperty(SCROLL_ZOOM_FACTOR);
        this.imageWidth = new SimpleDoubleProperty(0.0);
        this.imageHeight = new SimpleDoubleProperty(0.0);
        this.imageRotation = new SimpleDoubleProperty(0.0);
        this.image = new SimpleObjectProperty<>();

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.clipChildren(this);

        this.imageView.setOnMousePressed(e -> this.imageView.setCursor(Cursor.CLOSED_HAND));
        this.imageView.setOnMouseReleased(e -> this.imageView.setCursor(Cursor.OPEN_HAND));
        this.imageRotation.bind(this.imageView.rotateProperty());
        this.imageView.imageProperty().bind(this.image);

        this.zoomLabel.textProperty().bind(zoom
                .multiply(100)
                .asString(resources.getString("ImageViewer.statusBarZoomLevel.label")));

        this.image.addListener((ValueChangeListener<Image>) newImage -> {
            // reset image values
            this.imageWidth.unbind();
            this.imageWidth.setValue(0.0);
            this.imageHeight.unbind();
            this.imageHeight.setValue(0.0);

            if (newImage != null) {
                this.imageWidth.bind(newImage.widthProperty());
                this.imageHeight.bind(newImage.heightProperty());

                this.resetZoomAndPosition();
            }
        });

        MouseDragRecorder mouseDragRecorder = new MouseDragRecorder(this);
        mouseDragRecorder.dragRecordProperty().addListener((ValueChangeListener<MouseDragRecorder.DragRecord>) this::handleDragEvent);
    }

    public double getZoom() {
        return zoom.get();
    }

    public ReadOnlyDoubleProperty zoomProperty() {
        return zoom;
    }

    public double getScrollZoomFactor() {
        return scrollZoomFactor.get();
    }

    public DoubleProperty scrollZoomFactorProperty() {
        return scrollZoomFactor;
    }

    public void setScrollZoomFactor(double scrollZoomFactor) {
        this.scrollZoomFactor.set(scrollZoomFactor);
    }

    public double getImageRotation() {
        return imageRotation.get();
    }

    public ReadOnlyDoubleProperty imageRotationProperty() {
        return imageRotation;
    }

    public double getImageWidth() {
        return imageWidth.get();
    }

    public ReadOnlyDoubleProperty imageWidthProperty() {
        return imageWidth;
    }

    public double getImageHeight() {
        return imageHeight.get();
    }

    public ReadOnlyDoubleProperty imageHeightProperty() {
        return imageHeight;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public double getPaddedWidth() {
        return this.getWidth() - ZOOM_PADDING;
    }

    public double getPaddedHeight() {
        return this.getHeight() - ZOOM_PADDING;
    }

    public void resetZoomAndPosition() {
        if (imageView.getImage() != null) {
            // Zoom to fit in parent pane (if necessary)
            if (getImageWidth() > getPaddedWidth() || getImageHeight() > getPaddedHeight()) {
                zoomToFit();
            } else {
                // reset scale/zoom
                zoomToOriginalSize();
            }

            rotate(0.0);
        }
    }

    public void centerImage() {
        imageView.setTranslateX(-((getImageWidth() - getWidth()) / 2));
        imageView.setTranslateY(-((getImageHeight() - getHeight()) / 2));
    }

    public void zoomToOriginalSize() {
        centerImage();

        imageView.setScaleX(INITIAL_ZOOM);
        imageView.setScaleY(INITIAL_ZOOM);
        zoom.setValue(INITIAL_ZOOM);
    }

    public void zoomToFit() {
        zoomToOriginalSize();

        final double[] boundingBoxDim = Trigonometry2D.getBoundingBox(getImageRotation(), getImageWidth(), getImageHeight());

        final double xZoom = getPaddedWidth() / boundingBoxDim[0];
        final double yZoom = getPaddedHeight() / boundingBoxDim[1];

        if (xZoom < yZoom) {
            zoom(xZoom, null);
        } else {
            zoom(yZoom, null);
        }
    }

    public void rotateClockwise90() {
        final double imageRot = Trigonometry2D.rotate(getImageRotation(), Trigonometry2D.DEG_90);
        this.rotate(imageRot);
    }

    public void rotateCounterclockwise90() {
        final double imageRot = Trigonometry2D.rotate(getImageRotation(), Trigonometry2D.invertRotation(Trigonometry2D.DEG_90));
        this.rotate(imageRot);
    }

    public void rotate(double deg) {
        final double[] offsets = Trigonometry2D.rotateCoordinates(deg, 3, 7);
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

    private void handleDragEvent(MouseDragRecorder.DragRecord dragRecord) {
        // Get real start x,y after image translations
        final double realStartX = dragRecord.getStartX() - imageView.getTranslateX() - (getImageWidth() / 4.0);
        final double realStartY = dragRecord.getStartY() - imageView.getTranslateY() - (getImageHeight() / 4.0);

        // If drag is outside image then rotate else translate
        if (imageView.getBoundsInLocal().contains(realStartX, realStartY)) {
            imageView.setTranslateX(imageView.getTranslateX() + dragRecord.getDeltaX());
            imageView.setTranslateY(imageView.getTranslateY() + dragRecord.getDeltaY());
        } else {
            rotate(getImageRotation() + dragRecord.getDeltaY() / DRAG_ROTATION_DIVISOR);
        }
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
