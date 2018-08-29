package nl.juraji.imagemanager.util.collections;

import java.time.Duration;
import java.util.List;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class DurationAverageList extends AverageList<Duration> {

    public DurationAverageList(int sampleSize) {
        super(sampleSize, Duration.ZERO, DurationAverageList::generateAverage);
    }

    private static Duration generateAverage(List<Duration> durations, int sampleSize) {
        final double average = durations.stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0);

        return Duration.ofMillis((long) average / sampleSize);
    }
}
