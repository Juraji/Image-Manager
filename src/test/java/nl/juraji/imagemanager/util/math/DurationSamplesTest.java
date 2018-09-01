package nl.juraji.imagemanager.util.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
class DurationSamplesTest {

    private DurationSamples durationSamples;

    @BeforeEach
    public void setUp() {
        this.durationSamples = new DurationSamples(5, 3);
    }

    @Test
    public void getAverage() {
        durationSamples.add(Duration.ofMillis(64855));
        durationSamples.add(Duration.ofMillis(65616));
        durationSamples.add(Duration.ofMillis(65451));
        durationSamples.add(Duration.ofMillis(65846));
        durationSamples.add(Duration.ofMillis(15454));

        final Duration average = durationSamples.getAverage();
        assertEquals("PT55.444S", average.toString());
    }

    @Test
    public void getAverageSingleSample() {
        durationSamples.add(Duration.ofMillis(15454));

        final Duration average = durationSamples.getAverage();
        assertEquals("PT15.454S", average.toString());
    }

    @Test
    public void getCombined() {
        durationSamples.add(Duration.ofMillis(64855));
        durationSamples.add(Duration.ofMillis(65616));
        durationSamples.add(Duration.ofMillis(65451));
        durationSamples.add(Duration.ofMillis(65846));
        durationSamples.add(Duration.ofMillis(15454));

        final Duration combined = durationSamples.getCombined();
        assertEquals("PT4M37.222S", combined.toString());
    }

    @Test
    public void getMin() {
        durationSamples.add(Duration.ofMillis(64855));
        durationSamples.add(Duration.ofMillis(65616));
        durationSamples.add(Duration.ofMillis(65451));
        durationSamples.add(Duration.ofMillis(65846));
        durationSamples.add(Duration.ofMillis(15454));

        final Duration min = durationSamples.getMin();
        assertEquals("PT15.454S", min.toString());
    }

    @Test
    public void getMax() {
        durationSamples.add(Duration.ofMillis(64855));
        durationSamples.add(Duration.ofMillis(65616));
        durationSamples.add(Duration.ofMillis(65451));
        durationSamples.add(Duration.ofMillis(65846));
        durationSamples.add(Duration.ofMillis(15454));

        final Duration max = durationSamples.getMax();
        assertEquals("PT1M5.846S", max.toString());
    }
}