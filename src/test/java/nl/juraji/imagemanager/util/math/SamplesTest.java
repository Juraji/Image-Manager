package nl.juraji.imagemanager.util.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
class SamplesTest {

    private TestSamples samples;

    @BeforeEach
    public void setUp() {
        this.samples = new TestSamples();
    }

    @Test
    public void initialValues() {
        final Integer[] expected = {0, 0, 0, 0, 0};
        assertArrayEquals(expected, samples.getSamplesAsArray());
    }

    @Test
    public void add() {
        samples.add(1);
        assertArrayEquals(new Integer[]{0, 0, 0, 0, 1}, samples.getSamplesAsArray());
        samples.add(2);
        assertArrayEquals(new Integer[]{0, 0, 0, 1, 2}, samples.getSamplesAsArray());
        samples.add(3);
        assertArrayEquals(new Integer[]{0, 0, 1, 2, 3}, samples.getSamplesAsArray());
        samples.add(4);
        assertArrayEquals(new Integer[]{0, 1, 2, 3, 4}, samples.getSamplesAsArray());
        samples.add(5);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, samples.getSamplesAsArray());
    }

    @Test
    public void getSamples() {
        final ArrayList<Integer> expected = new ArrayList<>();
        expected.add(0);
        expected.add(0);
        expected.add(0);
        expected.add(0);
        expected.add(0);

        final List<Integer> sampleList = samples.getSamples();
        assertEquals(expected, sampleList);
    }

    @Test
    public void size() {
        assertEquals(5, samples.size());
    }

    @Test
    public void getSampleCount() {
        assertEquals(0, samples.getSampleCount());

        IntStream.range(0, 32).forEach(i -> samples.add(i));
        assertEquals(32, samples.getSampleCount());
    }

    @Test
    public void hasCompletedCycle() {
        // Cycle should not be completed at 0 samples
        assertFalse(samples.hasCompletedCycle());

        samples.add(0);
        for (int i = 1; i < 29; i++) {

            // Every third added sample should have completed a cycle
            if (i % 3 == 0) {
                assertTrue(samples.hasCompletedCycle());
            } else {
                assertFalse(samples.hasCompletedCycle());
            }

            samples.add(i);
        }
    }

    @Test
    public void reset() {
        samples.add(5);
        samples.add(564);
        samples.add(65484);

        samples.reset();

        assertEquals(0, samples.getSampleCount());
        assertArrayEquals(new Integer[]{0, 0, 0, 0, 0}, samples.getSamplesAsArray());
    }

    private class TestSamples extends Samples<Integer> {

        public TestSamples() {
            super(5, 3, 0);
        }

        @Override
        public Integer getAverage() {
            return null;
        }

        @Override
        public Integer getCombined() {
            return null;
        }

        @Override
        public Integer getMin() {
            return null;
        }

        @Override
        public Integer getMax() {
            return null;
        }
    }
}