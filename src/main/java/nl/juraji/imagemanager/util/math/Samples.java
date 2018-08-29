package nl.juraji.imagemanager.util.math;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public abstract class Samples<T> {
    private final T[] backingArray;
    private final int sampleSize;
    private final int cycleSize;
    private final T initialValue;
    private final AtomicInteger sampleCount;

    /**
     * A Sampling list
     *
     * @param sampleSize   The amount of samples to keep
     * @param cycleSize    The amount of samples to be produced before a cycle completes
     * @param initialValue An initial value to populate the samples with
     */
    public Samples(int sampleSize, int cycleSize, T initialValue) {
        //noinspection unchecked
        this.backingArray = (T[]) new Object[sampleSize];
        this.sampleSize = sampleSize;
        this.cycleSize = cycleSize;
        this.initialValue = initialValue;
        this.sampleCount = new AtomicInteger();

        this.reset();
    }

    /**
     * Add a sample to the list, shifting all samples back and deleting the first sample
     *
     * @param sample The sample to add
     */
    public void add(T sample) {
        final int lastIndex = sampleSize - 1;
        if (lastIndex > 0) {
            System.arraycopy(backingArray, 1, backingArray, 0, lastIndex);
        }

        backingArray[lastIndex] = sample;
        sampleCount.incrementAndGet();
    }

    /**
     * Get a list of all current samples
     *
     * @return A List of T
     */
    public List<T> getSamples() {
        return Arrays.asList(backingArray);
    }

    /**
     * Get an array of all current samples
     *
     * @return An array of T
     */
    public T[] getSamplesAsArray() {
        return backingArray.clone();
    }

    /**
     * Get the latest sample submitted
     *
     * @return The latest sample in the set
     */
    public T latestSample() {
        return backingArray[sampleSize - 1];
    }

    /**
     * Get the size of the list
     *
     * @return The amount of samples contained by this list
     */
    public int size() {
        return backingArray.length;
    }

    /**
     * Get the amount of samples added in total
     *
     * @return The amount of samples added since creation
     */
    public int getSampleCount() {
        return sampleCount.get();
    }

    /**
     * Check if a a cycle of samples has been completed
     *
     * @return True when the modulus of the amount of generated samples to the cycle size equals 0
     */
    public boolean hasCompletedCycle() {
        final int count = getSampleCount();
        return count > 0 && count % cycleSize == 0;
    }

    /**
     * Reset all samples back to the initial value and clear the sample counter
     */
    public void reset() {
        for (int i = 0; i < sampleSize; i++) {
            backingArray[i] = initialValue;
        }

        sampleCount.set(0);
    }

    /**
     * Get the average value
     *
     * @return An instance of T representing the sample average
     */
    public abstract T getAverage();

    /**
     * Get the combined value
     *
     * @return An instance of T representing the sample total
     */
    public abstract T getCombined();

    /**
     * Get the smallest sample value
     *
     * @return An instance of T representing the smallest sample
     */
    public abstract T getMin();

    /**
     * Get the largest sample value
     *
     * @return An instance of T representing the largest sample
     */
    public abstract T getMax();
}
