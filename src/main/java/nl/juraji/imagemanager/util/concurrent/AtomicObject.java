package nl.juraji.imagemanager.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public class AtomicObject<T> extends AtomicReference<T> {

    public boolean isSet() {
        return this.get() != null;
    }

    public boolean isEmpty() {
        return this.get() == null;
    }
}
