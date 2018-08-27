package nl.juraji.imagemanager.util.concurrent;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public abstract class QueueTask<R> extends javafx.concurrent.Task<R> {
    private final AtomicInteger progressCounter = new AtomicInteger(0);

    /**
     * Generate a title for this task
     * Used by {@link TaskQueueBuilder} to display in the progress dialog.
     * When the result of this method is equal to NULL no dialog will be shown.
     *
     * @return Task title
     */
    public abstract String getTaskTitle(ResourceBundle resources);

    protected void incrementProgress(int total) {
        final int next = this.progressCounter.incrementAndGet();
        updateProgress(next, total);
    }
}
