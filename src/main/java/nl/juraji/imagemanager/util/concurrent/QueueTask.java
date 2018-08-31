package nl.juraji.imagemanager.util.concurrent;

import javafx.concurrent.Task;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public abstract class QueueTask<R> extends Task<R> {
    private final AtomicLong knownMaxProgress = new AtomicLong(-1);
    private final AtomicLong previousProgress = new AtomicLong(-1);

    /**
     * Generate a title for this task
     * Used by {@link TaskQueueBuilder} to display in the progress dialog.
     * When the result of this method is equal to NULL no dialog will be shown.
     *
     * @return Task title
     */
    public abstract String getTaskTitle(ResourceBundle resources);

    @Override
    protected void updateProgress(double workDone, double max) {
        this.knownMaxProgress.set((long) max);
        this.previousProgress.set((long) workDone);
        super.updateProgress(workDone, max);
    }

    protected void updateProgress() {
        final long max = this.knownMaxProgress.get();
        final long workDone = this.previousProgress.incrementAndGet();
        super.updateProgress(workDone, max);
    }

    protected void resetProgress() {
        this.knownMaxProgress.set(-1);
        this.previousProgress.set(-1);
        super.updateProgress(-1, -1);
    }
}
