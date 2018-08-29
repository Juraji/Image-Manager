package nl.juraji.imagemanager.dialogs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;
import nl.juraji.imagemanager.util.collections.DurationAverageList;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class ETCText extends Text {
    public final SimpleDoubleProperty total = new SimpleDoubleProperty(-1);
    public final SimpleDoubleProperty current = new SimpleDoubleProperty(-1);
    private final String format;
    private final DurationAverageList durationsAverage = new DurationAverageList(5, 3);
    private Instant previousTime;

    public ETCText(String format) {
        this.format = format;
        total.addListener((observable, oldValue, newValue) -> this.updateETAText());
        current.addListener((observable, oldValue, newValue) -> this.updateETAText());
    }

    private void updateETAText() {
        final double total = getTotal();
        final double current = getCurrent();

        if (current < 0 || total < 0) {
            setText(null);
            durationsAverage.reset();
            previousTime = null;
        } else {
            if (previousTime == null) {
                previousTime = Instant.now();
            }

            final Instant now = Instant.now();
            Duration elapsedSincePrevious = Duration.between(previousTime, now);
            durationsAverage.add(elapsedSincePrevious);

            if (durationsAverage.hasCompletedCycle()) {
                final Duration estimatedRemaining = durationsAverage.getAverage()
                        .multipliedBy((long) (total - current));
                final String hms = DurationFormatUtils.formatDuration(estimatedRemaining.toMillis(), format, true);

                setText(hms);
            }

            previousTime = now;
        }
    }

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public SimpleDoubleProperty totalProperty() {
        return total;
    }

    public double getCurrent() {
        return current.get();
    }

    public void setCurrent(double current) {
        this.current.set(current);
    }

    public SimpleDoubleProperty currentProperty() {
        return current;
    }
}
