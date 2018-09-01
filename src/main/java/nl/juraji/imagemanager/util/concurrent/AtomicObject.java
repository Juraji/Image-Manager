package nl.juraji.imagemanager.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class AtomicObject<T> extends AtomicReference<T> {

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
        super.set(null);
    }
}
