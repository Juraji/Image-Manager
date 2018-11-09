package nl.juraji.imagemanager.ui.components;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import nl.juraji.imagemanager.util.fxevents.MouseDragRecorder;
import nl.juraji.imagemanager.util.fxevents.ValueChangeListener;
import nl.juraji.imagemanager.util.math.Trigonometry2D;
import nl.juraji.imagemanager.util.ui.UIUtils;
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

    private final DoubleProperty zoom;
    private final DoubleProperty scrollZoomFactor;
    private final DoubleProperty imageWidth;
    private final DoubleProperty imageHeight;
    private final DoubleProperty imageRotation;
    private final BooleanProperty fullScreenMode;
    private final ObjectProperty<ZoomStyle> zoomStyle;
    private final ObjectProperty<Image> image;

    @FXML
    private ImageView imageView;
    @FXML
    private Label zoomLabel;
    @FXML
    private Label zoomToOriginalButton;
    @FXML
    private Label zoomToFitButton;
    @FXML
    private Label toggleFullScreenButton;
    @FXML
    private VBox viewerControlsBox;

    public ImageViewer() {
        this.zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        this.scrollZoomFactor = new SimpleDoubleProperty(SCROLL_ZOOM_FACTOR);
        this.imageWidth = new SimpleDoubleProperty(0.0);
        this.imageHeight = new SimpleDoubleProperty(0.0);
        this.imageRotation = new SimpleDoubleProperty(0.0);
        this.fullScreenMode = new SimpleBooleanProperty(false);
        this.zoomStyle = new SimpleObjectProperty<>(ZoomStyle.AUTO);
        this.image = new SimpleObjectProperty<>();

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.clipChildren(this);

        this.imageRotation.bind(this.imageView.rotateProperty());
        this.imageView.imageProperty().bind(this.image);

        this.zoomLabel.textProperty().bind(zoom
                .multiply(100)
                .asString(resources.getString("ImageViewer.statusBarZoomLevel.label")));

        this.zoomToOriginalButton.styleProperty().bind(Bindings.createStringBinding(() -> {
            if (ZoomStyle.ORIGINAL.equals(this.zoomStyle.get())) {
                return "-fx-background-color: rgba(0,255,0,0.3); -fx-background-radius: 20;";
            } else {
                return "-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 20;";
            }
        }, this.zoomStyle));

        this.zoomToFitButton.styleProperty().bind(Bindings.createStringBinding(() -> {
            if (ZoomStyle.ZOOM_TO_FIT.equals(this.zoomStyle.get())) {
                return "-fx-background-color: rgba(0,255,0,0.3); -fx-background-radius: 20;";
            } else {
                return "-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 20;";
            }
        }, this.zoomStyle));

        this.toggleFullScreenButton.styleProperty().bind(Bindings.createStringBinding(() -> {
            if (this.fullScreenMode.get()) {
                return "-fx-background-color: rgba(0,255,0,0.3); -fx-background-radius: 20;";
            } else {
                return "-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 20;";
            }
        }, this.fullScreenMode));

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
        mouseDragRecorder.setDeadZoneNode(viewerControlsBox);
        mouseDragRecorder.dragRecordProperty().addListener((ValueChangeListener<MouseDragRecorder.DragRecord>) dragRecord -> {
            imageView.setTranslateX(imageView.getTranslateX() + dragRecord.getDeltaX());
            imageView.setTranslateY(imageView.getTranslateY() + dragRecord.getDeltaY());
        });
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

    public boolean isFullScreenMode() {
        return fullScreenMode.get();
    }

    public BooleanProperty fullScreenModeProperty() {
        return fullScreenMode;
    }

    public void toggleFullScreenMode() {
        this.fullScreenMode.setValue(this.fullScreenMode.not().get());
    }

    public void setFullScreenMode(boolean fullScreenMode) {
        this.fullScreenMode.set(fullScreenMode);
    }

    public ZoomStyle getZoomStyle() {
        return zoomStyle.get();
    }

    public ObjectProperty<ZoomStyle> zoomStyleProperty() {
        return zoomStyle;
    }

    public void setZoomStyle(ZoomStyle zoomStyle) {
        this.zoomStyle.set(zoomStyle);
    }

    public void resetViewer() {
        this.zoomStyle.set(ZoomStyle.AUTO);
        this.resetZoomAndPosition();
    }

    public void resetZoomAndPosition() {
        if (imageView.getImage() != null) {
            switch (zoomStyle.get()) {
                case ZOOM_TO_FIT:
                    zoomToFit();
                    break;
                case ORIGINAL:
                    zoomToOriginalSize();
                    break;
                case AUTO:
                    // Zoom to fit in parent pane (if necessary)
                    if (getImageWidth() > getPaddedWidth() || getImageHeight() > getPaddedHeight()) {
                        zoomToFit();
                    } else {
                        // reset scale/zoom
                        zoomToOriginalSize();
                    }
                    break;
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

        final Trigonometry2D.BoundingBox boundingBoxDim = Trigonometry2D.getBoundingBox(getImageRotation(), getImageWidth(), getImageHeight());

        final double xZoom = getPaddedWidth() / boundingBoxDim.getX();
        final double yZoom = getPaddedHeight() / boundingBoxDim.getY();

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
        final Trigonometry2D.BoundingBox offsets = Trigonometry2D.rotateCoordinates(deg, 3, 7);
        final DropShadow dropShadow = new DropShadow(10, offsets.getX(), offsets.getY(),
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
    private void zoomToOriginalSizeAction() {
        this.zoomStyle.setValue(ZoomStyle.ORIGINAL);
        this.zoomToOriginalSize();
    }

    @FXML
    private void zoomToFitAction() {
        this.zoomStyle.setValue(ZoomStyle.ZOOM_TO_FIT);
        this.zoomToFit();
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

    public enum ZoomStyle {
        ZOOM_TO_FIT, ORIGINAL, AUTO
    }
}
