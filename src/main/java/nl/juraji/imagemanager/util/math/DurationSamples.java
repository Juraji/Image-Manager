package nl.juraji.imagemanager.util.math;

import java.time.Duration;
import java.util.Comparator;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 *
 * A set of duration samples.
 * Calculations are at millisecond resolution
 */
public class DurationSamples extends Samples<Duration> {

    public DurationSamples(int sampleSize, int cycleSize) {
        super(sampleSize, cycleSize, Duration.ZERO);
    }

    @Override
    public Duration getAverage() {
        final double averageMillis = getSamples().stream()
                .filter(d -> !d.isZero())
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0);

        return Duration.ofMillis((long) averageMillis);
    }

    @Override
    public Duration getCombined() {
        return getSamples().stream()
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Duration getMin() {
        return getSamples().stream()
                .min(Comparator.comparingLong(Duration::toMillis))
                .orElse(Duration.ZERO);
    }

    @Override
    public Duration getMax() {
        return getSamples().stream()
                .max(Comparator.comparingLong(Duration::toMillis))
                .orElse(Duration.ZERO);
    }
}
