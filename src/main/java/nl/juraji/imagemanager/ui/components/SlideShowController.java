package nl.juraji.imagemanager.ui.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import nl.juraji.imagemanager.util.math.FXColors;
import nl.juraji.imagemanager.util.fxevents.ValueChangeListener;
import nl.juraji.imagemanager.util.ui.traits.FXMLConstructor;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Juraji on 8-9-2018.
 * Image Manager
 */
public class SlideShowController extends HBox implements FXMLConstructor, Initializable {
    private final AtomicObject<Timer> timerRef;
    private final ObjectProperty<EventHandler<SlideEvent>> onSlideEvent;
    private final BooleanProperty playing;
    private final BooleanProperty shuffleEnabled;

    @FXML
    private ImageView previousButton;
    @FXML
    private ImageView startButton;
    @FXML
    private ImageView stopButton;
    @FXML
    private ImageView nextButton;
    @FXML
    private ImageView shuffleButton;
    @FXML
    private Slider timerIntervalSlider;

    public SlideShowController() {
        this.timerRef = new AtomicObject<>(Timer::cancel);
        this.onSlideEvent = new SimpleObjectProperty<>();
        this.playing = new SimpleBooleanProperty(false);
        this.shuffleEnabled = new SimpleBooleanProperty(false);

        this.constructFXML();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.timerIntervalSlider.valueProperty().addListener((ValueChangeListener<Number>) newValue -> {
            if (this.playing.get()) {
                this.stop();
                if (newValue.intValue() > 0) {
                    this.start();
                }
            }
        });

        this.startButton.effectProperty().bind(Bindings.createObjectBinding(
                () -> playing.get() ? FXColors.colorAdjustEffect(Color.GREEN) : null,
                this.playing));
        this.shuffleButton.effectProperty().bind(Bindings.createObjectBinding(
                () -> shuffleEnabled.get() ? FXColors.colorAdjustEffect(Color.GREEN) : null,
                this.shuffleEnabled));
        this.previousButton.effectProperty().bind(Bindings.createObjectBinding(
                () -> shuffleEnabled.get() ? FXColors.colorAdjustEffect(Color.GRAY) : null,
                this.shuffleEnabled));
        this.previousButton.disableProperty().bind(this.shuffleEnabled);
    }

    public EventHandler<SlideEvent> getOnSlideEvent() {
        return onSlideEvent.get();
    }

    public ObjectProperty<EventHandler<SlideEvent>> onSlideEventProperty() {
        return onSlideEvent;
    }

    public void setOnSlideEvent(EventHandler<SlideEvent> onSlideEvent) {
        this.onSlideEvent.set(onSlideEvent);
    }

    public double getInterval() {
        return timerIntervalSlider.getValue();
    }

    public DoubleProperty intervalProperty() {
        return timerIntervalSlider.valueProperty();
    }

    public void setInterval(double integerValue) {
        timerIntervalSlider.setValue(integerValue);
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public ReadOnlyBooleanProperty playingProperty() {
        return playing;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled.get();
    }

    public BooleanProperty shuffleEnabledProperty() {
        return shuffleEnabled;
    }

    public void toggleShuffleEnabled() {
        this.shuffleEnabled.setValue(this.shuffleEnabled.not().get());
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        this.shuffleEnabled.set(shuffleEnabled);
    }

    public void start() {
        if (playing.not().get()) {
            if (onSlideEvent.isNull().get()) {
                // onSlideEvent event binding is mandatory
                throw new IllegalStateException("onSlideEvent event handler is not bound to method to execute!");
            }

            final int interval = (int) (timerIntervalSlider.getValue() * 1000);

            timerRef.set(new Timer("SlideShowController_IntervalTimer"));
            timerRef.get().scheduleAtFixedRate(new SlideShowTimerTask(), interval, interval);
            this.playing.setValue(true);
        }
    }

    public void stop() {
        timerRef.clear();
        this.playing.setValue(false);
    }

    @FXML
    private void startStopAction() {
        if (this.playing.get()) {
            this.stop();
        } else {
            this.start();
        }
    }

    @FXML
    private void previousAction() {
        this.onSlideEvent.get().handle(new SlideEvent(SlideEvent.PREVIOUS_SLIDE_EVENT));
    }

    @FXML
    private void nextAction() {
        if (this.shuffleEnabled.get()) {
            this.onSlideEvent.get().handle(new SlideEvent(SlideEvent.NEXT_RANDOM_SLIDE_EVENT));
        } else {
            this.onSlideEvent.get().handle(new SlideEvent(SlideEvent.NEXT_SLIDE_EVENT));
        }
    }

    public static class SlideEvent extends Event {
        public static final EventType<SlideEvent> SLIDE_EVENT = new EventType<>("slideEvent");
        public static final EventType<SlideEvent> PREVIOUS_SLIDE_EVENT = new EventType<>(SLIDE_EVENT, "previousSlideEvent");
        public static final EventType<SlideEvent> NEXT_SLIDE_EVENT = new EventType<>(SLIDE_EVENT, "nextSlideEvent");
        public static final EventType<SlideEvent> NEXT_RANDOM_SLIDE_EVENT = new EventType<>(SLIDE_EVENT, "nextRandomSlideEvent");

        public SlideEvent(EventType<SlideEvent> type) {
            super(type);
        }
    }

    private class SlideShowTimerTask extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(SlideShowController.this::nextAction);
        }
    }
}
