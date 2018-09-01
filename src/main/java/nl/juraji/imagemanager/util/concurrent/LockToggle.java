package nl.juraji.imagemanager.util.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Juraji on 1-9-2018.
 * Image Manager
 */
public class LockToggle {
    private boolean locked = false;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }
}
