package nl.juraji.imagemanager.ui.builders;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.FxCss;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class ToastBuilder {
    private static final AtomicInteger TOAST_COUNT = new AtomicInteger(0);
    private static final long MESSAGE_TIMEOUT = 3500;
    private static final long FADE_OUT_TIME = 300;
    private static final long MARGIN = 15;
    private static final double MAX_WIDTH = 500.0;

    private final Stage owner;
    private final Stage toastStage;
    private final Label text;
    private final VBox root;

    private ToastBuilder(Stage owner) {
        this.owner = owner;

        this.toastStage = new Stage();
        this.toastStage.initOwner(owner);
        this.toastStage.setResizable(false);
        this.toastStage.initStyle(StageStyle.TRANSPARENT);

        text = new Label();
        text.setTextFill(Color.WHITE);
        text.setWrapText(true);
        text.setMaxWidth(MAX_WIDTH);

        this.root = new VBox(5.0);
        root.getChildren().add(text);

        root.setStyle(FxCss.fontSize(14)
                + FxCss.padding(15)
                + FxCss.backgroundColor(0, 0, 0, 0.7));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        this.toastStage.setScene(scene);
    }

    public static ToastBuilder create() {
        return new ToastBuilder(Main.getPrimaryStage());
    }

    public static ToastBuilder create(Stage targetStage) {
        return new ToastBuilder(targetStage);
    }

    public ToastBuilder withMessage(String message, Object... params) {
        text.setText(TextUtils.format(message, params));
        return this;
    }

    public ToastBuilder withThrobber() {
        final ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        root.getChildren().add(0, indicator);
        return this;
    }

    public void show() {
        this.initPositionListeners();
        this.toastStage.show();
        this.playAnimationFrames();
    }

    private void initPositionListeners() {
        ChangeListener<Number> xListener = (observable, oldValue, newValue) ->
                toastStage.setX(newValue.doubleValue() + owner.getWidth() - (toastStage.getWidth() + MARGIN));
        ChangeListener<Number> yListener = (observable, oldValue, newValue) -> {
            double toastHeight = (toastStage.getHeight() + MARGIN) * TOAST_COUNT.incrementAndGet();
            toastStage.setY(newValue.doubleValue() + owner.getHeight() - toastHeight);
        };

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

    private void playAnimationFrames() {
        new Thread(() -> {
            try {
                Thread.sleep(MESSAGE_TIMEOUT);
            } catch (InterruptedException ignored) {
            }

            Timeline fadeOutTimeline = new Timeline();
            KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(FADE_OUT_TIME),
                    new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
            fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
            fadeOutTimeline.setOnFinished((aeb) -> {
                Platform.runLater(toastStage::close);
                TOAST_COUNT.decrementAndGet();
            });
            fadeOutTimeline.play();
        }).start();
    }
}
