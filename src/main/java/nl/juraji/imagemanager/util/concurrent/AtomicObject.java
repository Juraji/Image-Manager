package nl.juraji.imagemanager.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class AtomicObject<T> extends AtomicReference<T> {
    private final Consumer<T> beforeClear;

    public AtomicObject() {
        this(null);
    }

    public AtomicObject(Consumer<T> beforeClear) {
        this.beforeClear = beforeClear;
    }

    public void set(Supplier<T> objectSupplier) {
        super.set(objectSupplier.get());
    }

    public boolean isSet() {
        return this.get() != null;
    }

    public boolean isEmpty() {
        return this.get() == null;
    }

    public void clear() {
        if (this.beforeClear != null && this.isSet()) {
            this.beforeClear.accept(this.get());
        }

        super.set(null);
    }
}
