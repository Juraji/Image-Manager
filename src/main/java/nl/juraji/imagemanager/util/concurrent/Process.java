package nl.juraji.imagemanager.util.concurrent;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import nl.juraji.imagemanager.util.ResourceUtils;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 */
public abstract class Process<R> implements Callable<R> {
    private final AtomicDouble maxWork = new AtomicDouble(-1);
    private final AtomicDouble previousWork = new AtomicDouble(-1);
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);
    private final SimpleStringProperty title = new SimpleStringProperty();
    protected final ResourceBundle resources = ResourceUtils.getLocaleBundle();

    public DoubleProperty progressProperty() {
        return progress;
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    protected void updateProgress(Number workDone, Number max) {
        this.maxWork.set(max.longValue());
        this.previousWork.set(workDone.longValue());
        Platform.runLater(() -> progress.setValue(workDone.doubleValue() / max.doubleValue()));
    }

    protected void updateProgress() {
        final double max = this.maxWork.get();
        final double workDone = this.previousWork.addAndGet(1.0);
        this.updateProgress(workDone, max);
    }

    protected void setMaxProgress(Number max) {
        this.maxWork.set(max.doubleValue());
    }

    protected void addToMaxProgress(Number deltaWorkDone) {
        this.maxWork.addAndGet(deltaWorkDone.doubleValue());
        this.updateProgress();
    }

    protected void resetProgress() {
        this.maxWork.set(-1);
        this.previousWork.set(-1);
        this.updateProgress(-1, -1);
    }
}
