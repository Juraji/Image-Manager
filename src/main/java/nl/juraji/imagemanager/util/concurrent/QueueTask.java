package nl.juraji.imagemanager.util.concurrent;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public abstract class QueueTask<R> extends javafx.concurrent.Task<R> {
    private final AtomicLong progressCounter = new AtomicLong(0);

    /**
     * Generate a title for this task
     * Used by {@link TaskQueueBuilder} to display in the progress dialog.
     * When the result of this method is equal to NULL no dialog will be shown.
     *
     * @return Task title
     */
    public abstract String getTaskTitle(ResourceBundle resources);

    protected void incrementProgress(long total) {
        final long next = this.progressCounter.incrementAndGet();
        updateProgress(next, total);
    }

    protected void incrementProgress(long delta, long total) {
        final long next = this.progressCounter.addAndGet(delta);
        updateProgress(next, total);
    }

    @Override
    protected void updateProgress(long workDone, long max) {
        this.progressCounter.set(workDone);
        super.updateProgress(workDone, max);
    }

    @Override
    protected void updateProgress(double workDone, double max) {
        this.progressCounter.set((long) workDone);
        super.updateProgress(workDone, max);
    }

    protected void restartProgress() {
        this.progressCounter.set(0);
        updateProgress(-1, -1);
    }
}
