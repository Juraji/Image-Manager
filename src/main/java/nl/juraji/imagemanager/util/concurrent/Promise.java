package nl.juraji.imagemanager.util.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class Promise<T> {
    private final CompletableFuture<T> future;

    public Promise(Resolver<T> resolve) {
        this.future = new CompletableFuture<>();
        resolve.accept(future::complete);
    }

    public void then(Consumer<T> consumer) {
        future.whenComplete((t, e) -> {
            if (e == null) {
                consumer.accept(t);
            }
        });
    }

    public void except(Consumer<Throwable> consumer) {
        future.whenComplete((t, e) -> {
            if (e != null) {
                consumer.accept(e);
            }
        });
    }

    public interface Resolver<T> extends Consumer<Consumer<T>> {
    }
}
