package nl.juraji.imagemanager.util.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.TextUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public final class ProcessChainBuilder implements Runnable {
    private static final LockToggle TASK_LOCK = new LockToggle();

    private final LinkedList<ProcessExecution> taskChain;
    private final ResourceBundle resources;
    private final HashSet<Runnable> succeededTasks;
    private final Logger logger;

    /**
     * Chain multiple {@link Process} so they execute consecutively.
     * Once built, {@link #run} method can be called multiple times safely.
     *
     * @param resources The current i18n resource bundle
     */
    private ProcessChainBuilder(ResourceBundle resources) throws TaskInProgressException {
        if (TASK_LOCK.isLocked()) {
            throw new TaskInProgressException();
        }

        this.resources = resources;
        this.taskChain = new LinkedList<>();
        this.succeededTasks = new HashSet<>();
        this.logger = Log.create(this);
    }

    public static ProcessChainBuilder create(ResourceBundle resources) throws TaskInProgressException {
        return new ProcessChainBuilder(resources);
    }

    public <R> ProcessChainBuilder appendTask(Process<R> nextTask) {
        return appendTask(nextTask, null, null);
    }

    public <R> ProcessChainBuilder appendTask(Process<R> nextTask, Consumer<R> onTaskResult) {
        return appendTask(nextTask, onTaskResult, null);
    }

    public <R> ProcessChainBuilder appendTask(Process<R> nextTask, Consumer<R> onTaskResult, Consumer<Throwable> onException) {
        taskChain.add(new ProcessExecution<>(nextTask, onTaskResult, onException));
        return this;
    }

    public ProcessChainBuilder onSucceeded(Runnable handler) {
        this.succeededTasks.add(handler);
        return this;
    }

    @Override
    public void run() {
        TASK_LOCK.lock();

        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                for (ProcessExecution execution : taskChain) {
                    final Process task = execution.process;
                    final String taskTitle = task.getTaskTitle(resources);
                    logger.info("Running task " + taskTitle);

                    if (!TextUtils.isEmpty(taskTitle)) {
                        Platform.runLater(() -> Main.getPrimaryScene().activateProgressBar(task));
                    }

                    task.run();
                    Platform.runLater(() -> {
                        final Throwable exception = task.getException();
                        if (exception != null) {
                            logger.error("Error during task " + taskTitle, exception);
                            execution.emitException(exception);
                        } else {
                            final Object value = task.getValue();

                            if (value != null) {
                                if (value instanceof Collection) {
                                    final int size = ((Collection) value).size();
                                    if (size > 0) {
                                        logger.info("Task done, " + size + " values emitted");
                                    }
                                } else {
                                    logger.info("Task done, value emitted: " + value);
                                }
                            } else {
                                logger.info("Task done, emitting result as null");
                            }

                            // Always emit a result, even if it's NULL
                            //noinspection unchecked
                            execution.emitTaskResult(value);
                        }
                    });
                }

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            //noinspection unchecked
            succeededTasks.forEach(Runnable::run);
        });

        task.runningProperty().addListener((o, i, isRunning) -> {
            if (!isRunning) {
                TASK_LOCK.unlock();
            }
        });

        new Thread(task).start();
    }

    public class TaskInProgressException extends Exception {
        public TaskInProgressException() {
            super("A task is already running");
        }
    }

    private class ProcessExecution<R> {
        private final Process<R> process;
        private final Consumer<R> onTaskResult;
        private Consumer<Throwable> onException;

        private ProcessExecution(Process<R> process,
                                 Consumer<R> onTaskResult,
                                 Consumer<Throwable> onException) {
            this.process = process;
            this.onTaskResult = onTaskResult;
            this.onException = onException;
        }

        public void emitTaskResult(R value) {
            if (this.onTaskResult != null) {
                this.onTaskResult.accept(value);
            }
        }

        public void emitException(Throwable exception) {
            if (this.onException != null) {
                this.onException.accept(exception);
            }
        }
    }
}
