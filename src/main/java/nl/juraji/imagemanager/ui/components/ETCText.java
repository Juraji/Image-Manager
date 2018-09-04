package nl.juraji.imagemanager.ui.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Text;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import nl.juraji.imagemanager.util.math.DurationSamples;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class ETCText extends Text {
    private static final double MAX_PERCENT = 100.0;
    private static final String LABEL_FORMAT = "ETC: %02d:%02d:%02d (%d%%)";

    private final DurationSamples durationSamples = new DurationSamples(5, 1);
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);
    private final AtomicObject<Instant> previousTimeRef = new AtomicObject<>();
    private final AtomicInteger previousProgress = new AtomicInteger(-1);
    private final Duration minRemainingTime;

    /**
     * An automated Estimated Time Completed text node (ETC: HH:mm:ss (XX%))
     */
    public ETCText() {
        this.minRemainingTime = Duration.ofSeconds(10);
        this.progress.addListener(this::handleProgress);
    }

    public void setProgress(Number progress) {
        this.progress.set(progress.doubleValue());
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    @SuppressWarnings("unused")
    private void handleProgress(ObservableValue<? extends Number> observable, Number oldProgress, Number progress) {
        final double realProgress = progress.doubleValue() * MAX_PERCENT;
        final int roundedProgress = (int) realProgress;

        if (realProgress < 0) {
            setVisible(false);
            setText(null);

            durationSamples.reset();
            previousTimeRef.clear();
            previousProgress.set(-1);
        } else if (previousProgress.get() < roundedProgress) {
            this.updateETC(realProgress);
            previousProgress.set(roundedProgress);
        }
    }

    private void updateETC(double progress) {
        if (previousTimeRef.isEmpty()) {
            previousTimeRef.set(Instant.now());
        }

        final Instant now = Instant.now();
        Duration elapsedSincePrevious = Duration.between(previousTimeRef.get(), now);
        durationSamples.add(elapsedSincePrevious);

        final long estimatedRemaining = durationSamples.getAverage()
                .multipliedBy((long) (MAX_PERCENT - progress))
                .toMillis();

        if (!isVisible() && estimatedRemaining > minRemainingTime.toMillis()) {
            setVisible(true);
        }

        if (isVisible()) {
            final String hms = String.format(LABEL_FORMAT,
                    TimeUnit.MILLISECONDS.toHours(estimatedRemaining) % 24,
                    TimeUnit.MILLISECONDS.toMinutes(estimatedRemaining) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(estimatedRemaining) % 60,
                    ((int) progress));

            setText(hms);
        }

        previousTimeRef.set(now);
    }
}
