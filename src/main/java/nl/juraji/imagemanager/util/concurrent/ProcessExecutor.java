package nl.juraji.imagemanager.util.concurrent;

import javafx.application.Platform;
import javafx.beans.property.*;
import nl.juraji.imagemanager.util.Log;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by Juraji on 12-9-2018.
 * Image Manager
 */
public class ProcessExecutor {
    private final ExecutorService executor;
    private final SimpleBooleanProperty processRunning;
    private final SimpleStringProperty processTitle;
    private final SimpleDoubleProperty progress;
    private final SimpleIntegerProperty queueCount;
    private final Logger logger;

    public ProcessExecutor() {
        this.queueCount = new SimpleIntegerProperty(0);
        this.executor = Executors.newSingleThreadExecutor();
        this.processRunning = new SimpleBooleanProperty(false);
        this.processTitle = new SimpleStringProperty();
        this.progress = new SimpleDoubleProperty(-1);
        this.logger = Log.create(this);
    }

    public SimpleBooleanProperty processRunningProperty() {
        return processRunning;
    }

    public SimpleStringProperty processTitleProperty() {
        return processTitle;
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    public SimpleIntegerProperty queueCountProperty() {
        return queueCount;
    }

    public void submitIntermediateFunction(Runnable runnable) {
        this.executor.submit(runnable);
    }

    public <T> void submitProcess(Process<T> process) {
        this.submitProcess(process, null, null);
    }

    public <T> void submitProcess(Process<T> process, Consumer<T> onTaskResult) {
        this.queueCount.setValue(this.queueCount.get() + 1);
        this.submitProcess(process, onTaskResult, null);
    }

    public <T> void submitProcess(Process<T> process, Consumer<T> onTaskResult, Consumer<Throwable> onException) {
        this.executor.submit(() -> {
            // Step 1: Bind and update properties to process
            runLater(() -> {
                this.processRunning.setValue(true);
                this.queueCount.setValue(this.queueCount.get() - 1);
                this.bindProcessTitleProperty(process.titleProperty());
                this.bindProcessProgressProperty(process.progressProperty());
            });

            // Step 2: Run actual process
            logger.info("Running process: " + process.getTitle());
            final CompletableFuture<T> future = new CompletableFuture<>();
            try {
                future.complete(process.call());
            } catch (Throwable e) {
                logger.error("Error during task " + process.getTitle(), e);
                future.completeExceptionally(e);
            }

            // Step 3: Handle task result
            // (optional task result is passed to onResult)
            // (pass any exception to onException)
            runLater(() -> this.processRunning.setValue(false));

            final String processTitle = process.getTitle();
            future.whenComplete((result, exception) -> {
                if (exception != null && onException != null) {
                    runLater(() -> onException.accept(exception));
                } else if (onTaskResult != null) {
                    runLater(() -> onTaskResult.accept(result));
                    logger.info("Process completed: " + processTitle + ", value emitted to onTaskResult");
                } else {
                    logger.info("Process completed: " + processTitle);
                }
            });
        });
    }

    private void bindProcessTitleProperty(ReadOnlyStringProperty titleProperty) {
        this.processTitle.unbind();
        this.processTitle.setValue(null);
        this.processTitle.bind(titleProperty);
    }

    private void bindProcessProgressProperty(ReadOnlyDoubleProperty progressProperty) {
        this.progress.unbind();
        this.progress.setValue(-1);
        this.progress.bind(progressProperty);
    }
}
