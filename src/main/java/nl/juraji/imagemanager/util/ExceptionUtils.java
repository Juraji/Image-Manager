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
     * Catches ALL {@link Throwable} and rethrows as {@link RuntimeException}
     * @param context A runnable
     */
    public static <E extends Throwable> void catchAll(RunnableExceptionContext<E> context) {
        try {
            context.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Convenience method
     * Catches ALL {@link Throwable} and rethrows as {@link RuntimeException}
     * @param context A Supplier
     */
    public static <T, E extends Throwable> T catchAll(CallableExceptionContext<T, E> context) {
        try {
            return context.get();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @FunctionalInterface
    public interface RunnableExceptionContext<E extends Throwable> {
        void run() throws E;
    }

    @FunctionalInterface
    public interface CallableExceptionContext<T, E extends Throwable> {
        T get() throws E;
    }
}
