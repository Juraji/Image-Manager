package nl.juraji.imagemanager.util.math;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class DurationSamples extends Samples<Duration> {
    private ToLongFunction<Duration> toLongFunction = Duration::toNanos;
    private Function<Long, Duration> fromLongFunction = Duration::ofNanos;

    public DurationSamples(int sampleSize, int cycleSize) {
        super(sampleSize, cycleSize, Duration.ZERO);
    }

    /**
     * Set the calculation precision
     * Possible options are MILLIS, SECONDS, MINUTES.
     * Defaults to nanoseconds (if null or any other ChronoUnit).
     * @param chronoUnit The ChronoUnit to use as precision
     * @throws NullPointerException When chronoUnit equals null
     */
    public void setPrecision(ChronoUnit chronoUnit) {
        switch (chronoUnit) {
            case MILLIS:
                toLongFunction = Duration::toMillis;
                fromLongFunction = Duration::ofMillis;
                break;
            case SECONDS:
                toLongFunction = d -> d.toMillis() / 1000;
                fromLongFunction = Duration::ofSeconds;
                break;
            case MINUTES:
                toLongFunction = Duration::toMinutes;
                fromLongFunction = Duration::ofMinutes;
                break;
            default:
                toLongFunction = Duration::toNanos;
                fromLongFunction = Duration::ofNanos;
                break;
        }
    }

    @Override
    public Duration getAverage() {
        final double averageNano = getSamples().stream()
                .filter(d -> !d.isZero())
                .mapToLong(toLongFunction)
                .average()
                .orElse(0);

        return fromLongFunction.apply((long) averageNano);
    }

    @Override
    public Duration getCombined() {
        return getSamples().stream()
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Duration getMin() {
        return getSamples().stream()
                .min(Comparator.comparingLong(toLongFunction))
                .orElse(Duration.ZERO);
    }

    @Override
    public Duration getMax() {
        return getSamples().stream()
                .max(Comparator.comparingLong(toLongFunction))
                .orElse(Duration.ZERO);
    }
}
