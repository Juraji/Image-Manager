package nl.juraji.imagemanager.util.collections;

import java.util.List;

/**
 * Created by Juraji on 29-8-2018.
 * Image Manager
 */
public class IntegerAverageList extends AverageList<Integer> {
    public IntegerAverageList(int sampleSize, int cycleSize) {
        super(sampleSize, cycleSize, 0, IntegerAverageList::generateAverage);
    }

    private static Integer generateAverage(List<Integer> integers, Integer total) {
        return (int) integers.stream()
                .mapToInt(i->i)
                .average()
                .orElse(0) / total;
    }
}
