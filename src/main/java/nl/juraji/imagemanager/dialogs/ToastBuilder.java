package nl.juraji.imagemanager.dialogs;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import nl.juraji.imagemanager.util.TextUtils;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class ToastBuilder {
    private static final long MESSAGE_TIMEOUT = 3500;
    private static final long FADE_IN_TIME = 300;
    private static final long FADE_OUT_TIME = 300;
    private static final long MARGIN = 20;

    private final Stage owner;
    private final Stage toastStage;
    private final Text text;

    private ToastBuilder(Stage owner) {
        this.owner = owner;

        this.toastStage = new Stage();
        this.toastStage.initOwner(owner);
        this.toastStage.setResizable(false);
        this.toastStage.initStyle(StageStyle.TRANSPARENT);

        text = new Text();
        text.setFill(Color.WHITE);

        VBox root = new VBox(5.0);
        root.getChildren().add(text);
        root.setStyle("-fx-font-size: 14px; -fx-padding: 15px; -fx-background-color: rgba(0, 0, 0, 0.7);");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        this.toastStage.setScene(scene);
    }

    public static ToastBuilder create(Stage owner) {
        return new ToastBuilder(owner);
    }

    public ToastBuilder withMessage(String message, Object... params) {
        text.setText(TextUtils.format(message, params));
        return this;
    }

    public ToastBuilder withThrobber() {
        VBox root = (VBox) this.toastStage.getScene().getRoot();
        final ProgressIndicator indicator = new ProgressIndicator();
        root.getChildren().add(0, indicator);
        return this;
    }

    public void queue() {
        this.initPositionListeners();
        this.toastStage.show();
        this.playFadeInFrames().setOnFinished(e -> new Thread(() -> {
            try {
                Thread.sleep(MESSAGE_TIMEOUT);
            } catch (InterruptedException ignored) {
            }
            playFadeOutFrames();
        }).start());
    }

    public ToastRef showAndKeep() {
        this.initPositionListeners();
        this.toastStage.show();
        return this::playFadeOutFrames;
    }

    private void initPositionListeners() {
        ChangeListener<Number> xListener = (observable, oldValue, newValue) ->
                toastStage.setX(newValue.doubleValue() + owner.getWidth() - (toastStage.getWidth() + MARGIN));
        ChangeListener<Number> yListener = (observable, oldValue, newValue) ->
                toastStage.setY(newValue.doubleValue() + owner.getHeight() - ((toastStage.getHeight() + MARGIN)));

        owner.xProperty().addListener(xListener);
        owner.yProperty().addListener(yListener);

        toastStage.setOnShown(e -> {
            xListener.changed(null, null, owner.getX());
            yListener.changed(null, null, owner.getY());
        });

        //Once the window is visible, remove the listeners
        toastStage.setOnCloseRequest(e -> {
            owner.xProperty().removeListener(xListener);
            owner.yProperty().removeListener(yListener);
        });
    }

    private Timeline playFadeInFrames() {
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(FADE_IN_TIME),
                new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        fadeInTimeline.play();
        return fadeInTimeline;
    }

    private void playFadeOutFrames() {
        Timeline fadeOutTimeline = new Timeline();
        KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(FADE_OUT_TIME),
                new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
        fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
        fadeOutTimeline.setOnFinished((aeb) -> {
            Platform.runLater(toastStage::close);
        });
        fadeOutTimeline.play();
    }

    public interface ToastRef {
        void close();
    }
}
