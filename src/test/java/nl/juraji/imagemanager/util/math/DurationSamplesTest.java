package nl.juraji.imagemanager.util.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
    public void setPrecision() {
        durationSamples.add(Duration.ofNanos(100));
        durationSamples.add(Duration.ofSeconds(54));
        durationSamples.add(Duration.ofNanos(5465464));
        durationSamples.add(Duration.ofMillis(762154544));

        // Nano precision by default
        assertEquals("PT52H55M52.137366391S", durationSamples.getAverage().toString());

        // Apply precision to calculations
        durationSamples.setPrecision(ChronoUnit.SECONDS);
        assertEquals("PT52H55M52S", durationSamples.getAverage().toString());
        assertEquals("PT211H43M28.549465564S", durationSamples.getCombined().toString());
        assertEquals("PT0S", durationSamples.getMin().toString());
        assertEquals("PT211H42M34.544S", durationSamples.getMax().toString());

        // Fall back to nano precision when incompatible ChronoUnit is supplied
        durationSamples.setPrecision(ChronoUnit.HOURS);
        assertEquals("PT52H55M52.137366391S", durationSamples.getAverage().toString());
    }

    @Test
    public void getAverage() {
        durationSamples.add(Duration.ofNanos(64855));
        durationSamples.add(Duration.ofNanos(65616));
        durationSamples.add(Duration.ofNanos(65451));
        durationSamples.add(Duration.ofNanos(65846));
        durationSamples.add(Duration.ofNanos(15454));

        final Duration average = durationSamples.getAverage();
        assertEquals("PT0.000055444S", average.toString());
    }

    @Test
    public void getAverageOneEntry() {
        durationSamples.add(Duration.ofNanos(15454));

        final Duration average = durationSamples.getAverage();
        assertEquals("PT0.000015454S", average.toString());
    }

    @Test
    public void getCombined() {
        durationSamples.add(Duration.ofNanos(64855));
        durationSamples.add(Duration.ofNanos(65616));
        durationSamples.add(Duration.ofNanos(65451));
        durationSamples.add(Duration.ofNanos(65846));
        durationSamples.add(Duration.ofNanos(15454));

        final Duration combined = durationSamples.getCombined();
        assertEquals("PT0.000277222S", combined.toString());
    }

    @Test
    public void getMin() {
        durationSamples.add(Duration.ofNanos(64855));
        durationSamples.add(Duration.ofNanos(65616));
        durationSamples.add(Duration.ofNanos(65451));
        durationSamples.add(Duration.ofNanos(65846));
        durationSamples.add(Duration.ofNanos(15454));

        final Duration min = durationSamples.getMin();
        assertEquals("PT0.000015454S", min.toString());
    }

    @Test
    public void getMax() {
        durationSamples.add(Duration.ofNanos(64855));
        durationSamples.add(Duration.ofNanos(65616));
        durationSamples.add(Duration.ofNanos(65451));
        durationSamples.add(Duration.ofNanos(65846));
        durationSamples.add(Duration.ofNanos(15454));

        final Duration max = durationSamples.getMax();
        assertEquals("PT0.000065846S", max.toString());
    }
}