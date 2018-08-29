package nl.juraji.imagemanager.dialogs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;
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
    private final DurationSamples durationsAverage;
    private final String format;
    private Instant previousTime;

    public ETCText(String prefix) {
        this.format = prefix + "%02d:%02d:%02d";
        this.durationsAverage = new DurationSamples(5, 1);

        this.progress.addListener((observable, oldValue, newValue) -> this.updateETC());
    }

    private void updateETC() {
        final double progress = getProgress();

        if (progress < 0) {
            setText(null);
            durationsAverage.reset();
            previousTime = null;
        } else {
            final double total = 100.0;
            final double current = progress * total;

            if (current % 1 == 0) {
                if (previousTime == null) {
                    previousTime = Instant.now();
                }

                final Instant now = Instant.now();
                Duration elapsedSincePrevious = Duration.between(previousTime, now);
                durationsAverage.add(elapsedSincePrevious);

                if (durationsAverage.hasCompletedCycle()) {
                    final long estimatedRemaining = durationsAverage.getAverage()
                            .multipliedBy((long) (total - current))
                            .toMillis();

                    final String hms = String.format(format, TimeUnit.MILLISECONDS.toHours(estimatedRemaining),
                            TimeUnit.MILLISECONDS.toMinutes(estimatedRemaining) % 60000,
                            TimeUnit.MILLISECONDS.toSeconds(estimatedRemaining) % 1000);

                    setText(hms);
                }

                previousTime = now;
            }
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
