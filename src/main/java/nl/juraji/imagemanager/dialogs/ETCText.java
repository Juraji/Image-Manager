package nl.juraji.imagemanager.dialogs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;
import nl.juraji.imagemanager.util.concurrent.AtomicObject;
import nl.juraji.imagemanager.util.math.DurationSamples;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class ETCText extends Text {
    public final SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);
    private final DurationSamples durationSamples;
    private final String format;
    private final Duration minTime;
    private final AtomicObject<Instant> previousTime;

    /**
     * An automated Estimated Time Completed text node (HH:mm:ss)
     *
     * @param prefix  The text to show before the ETC (e.g. "ETC: ")
     * @param minTime The minimum of remaining time before showing the text
     */
    public ETCText(String prefix, Duration minTime) {
        this.format = prefix + "%02d:%02d:%02d";
        this.minTime = minTime;
        this.durationSamples = new DurationSamples(5, 1);
        this.previousTime = new AtomicObject<>();

        this.progress.addListener((observable, oldValue, newValue) -> this.updateETC());
    }

    private void updateETC() {
        final double progress = getProgress();

        if (progress < 0) {
            setVisible(false);
            setText(null);
            durationSamples.reset();
            previousTime.set(null);
        } else {
            final double total = 100.0;
            final double current = progress * total;

            if (previousTime.isEmpty()) {
                previousTime.set(Instant.now());
            }

            final Instant now = Instant.now();
            Duration elapsedSincePrevious = Duration.between(previousTime.get(), now);
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

            previousTime.set(now);
        }
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
}
