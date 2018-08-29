package nl.juraji.imagemanager.util.collections;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class AverageList<T> {
    private final T[] backingArray;
    private final int cycleSize;
    private final BiFunction<List<T>, Integer, T> averageFunction;
    private final AtomicInteger sampleCount;
    private final int lastIndex;

    /**
     * A Sampling list
     *
     * @param sampleSize      The amount of samples to keep
     * @param cycleSize       The amount of samples to be produced before a cycle completes
     * @param initialValue    An initial value to populate the samples with
     * @param averageFunction A BiFunction that accepts a list of samples and the sample count
     */
    public AverageList(int sampleSize, int cycleSize, T initialValue, BiFunction<List<T>, Integer, T> averageFunction) {
        //noinspection unchecked
        this.backingArray = (T[]) new Object[sampleSize];
        this.lastIndex = sampleSize - 1;
        this.cycleSize = cycleSize;
        this.averageFunction = averageFunction;
        this.sampleCount = new AtomicInteger(0);

        for (int i = 0; i < sampleSize; i++) {
            backingArray[i] = initialValue;
        }
    }

    /**
     * Add a sample to the list, shifting all samples back and deleting the first sample
     *
     * @param sample The sample to add
     */
    public void add(T sample) {
        if (lastIndex >= 0) System.arraycopy(backingArray, 1, backingArray, 0, lastIndex);
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
     * Get the average value as calculated by averageFunction
     *
     * @return An instance of T representing the average of all samples
     */
    public T getAverage() {
        return averageFunction.apply(getSamples(), size());
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
        return getSampleCount() % cycleSize == 0;
    }
}
