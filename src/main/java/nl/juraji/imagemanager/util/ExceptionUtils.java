package nl.juraji.imagemanager.util;

/**
 * Created by Juraji on 20-8-2018.
 * Image Manager
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    /**
     * Convenience method
     * Catches ALL throwable and rethrows as unchecked
     * @param context A runnable
     */
    public static void catchAll(RuntimeExceptionContext context) {
        try {
            context.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @FunctionalInterface
    public interface RuntimeExceptionContext<E extends Throwable> {
        void run() throws E;
    }
}
