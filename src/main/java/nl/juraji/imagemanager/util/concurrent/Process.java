package nl.juraji.imagemanager.util.concurrent;

import javafx.concurrent.Task;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public abstract class Process<R> extends Task<R> {
    private final AtomicLong maxWork = new AtomicLong(-1);
    private final AtomicLong previousWork = new AtomicLong(-1);

    /**
     * Generate a title for this task
     * Used by {@link ProcessChainBuilder} to display in the progress dialog.
     * When the result of this method is equal to NULL no dialog will be shown.
     *
     * @return Task title
     */
    public abstract String getTaskTitle(ResourceBundle resources);

    @Override
    protected void updateProgress(double workDone, double max) {
        this.maxWork.set((long) max);
        this.previousWork.set((long) workDone);
        super.updateProgress(workDone, max);
    }

    protected void updateProgress() {
        final long max = this.maxWork.get();
        final long workDone = this.previousWork.incrementAndGet();
        super.updateProgress(workDone, max);
    }

    protected void setMaxProgress(long max) {
        this.maxWork.set(max);
    }

    protected void addToMaxProgress(long delta) {
        this.maxWork.addAndGet(delta);
        this.updateProgress();
    }

    protected void resetProgress() {
        this.maxWork.set(-1);
        this.previousWork.set(-1);
        super.updateProgress(-1, -1);
    }
}
