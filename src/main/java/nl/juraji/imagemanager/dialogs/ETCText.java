package nl.juraji.imagemanager.dialogs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Text;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import nl.juraji.imagemanager.util.math.DurationSamples;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class ETCText extends Text {
    private final DurationSamples durationSamples = new DurationSamples(5, 1);
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);
    private final AtomicObject<Instant> previousTimeRef = new AtomicObject<>();
    private final AtomicObject<Timer> timerRef = new AtomicObject<>();
    private final Duration minTime;
    private final String format;

    /**
     * An automated Estimated Time Completed text node (HH:mm:ss)
     *
     * @param prefix  The text to show before the ETC (e.g. "ETC: ")
     * @param minTime The minimum of remaining time before showing the text
     */
    public ETCText(String prefix, Duration minTime) {
        this.format = prefix + "%02d:%02d:%02d";
        this.minTime = minTime;

        this.progress.addListener(this::handleProgress);
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(Number progress) {
        this.progress.set(progress.doubleValue());
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    @SuppressWarnings("unused")
    private void handleProgress(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue.intValue() < 0) {
            setVisible(false);
            setText(null);

            durationSamples.reset();
            previousTimeRef.set(null);

            if (timerRef.isSet()) {
                timerRef.get().cancel();
                timerRef.clear();
            }
        } else if (timerRef.isEmpty()) {
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(createETCTimerTask(), 0, 1000);
            timerRef.set(timer);
        }
    }

    private TimerTask createETCTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                final double progress = getProgress();

                final double total = 100.0;
                final double current = progress * total;

                if (previousTimeRef.isEmpty()) {
                    previousTimeRef.set(Instant.now());
                }

                final Instant now = Instant.now();
                Duration elapsedSincePrevious = Duration.between(previousTimeRef.get(), now);
                durationSamples.add(elapsedSincePrevious);

                if (durationSamples.hasCompletedCycle()) {
                    final long estimatedRemaining = durationSamples.getAverage()
                            .multipliedBy((long) (total - current))
                            .toMillis();

                    if (!isVisible() && estimatedRemaining > minTime.toMillis()) {
                        setVisible(true);
                    }

                    if (isVisible()) {
                        final String hms = String.format(format,
                                TimeUnit.MILLISECONDS.toHours(estimatedRemaining) % 24,
                                TimeUnit.MILLISECONDS.toMinutes(estimatedRemaining) % 60,
                                TimeUnit.MILLISECONDS.toSeconds(estimatedRemaining) % 60);

                        setText(hms);
                    }
                }

                previousTimeRef.set(now);
            }
        };
    }
}
