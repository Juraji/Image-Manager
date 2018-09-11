package nl.juraji.imagemanager.util.math;

/**
 * Created by Juraji on 12-9-2018.
 * Image Manager
 */
public final class PagingMath {
    private PagingMath() {
    }

    public static int pageCount(Number itemCount, Number pageSize) {
        return (int) Math.ceil(itemCount.doubleValue() / pageSize.doubleValue());
    }
}
