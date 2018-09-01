package nl.juraji.imagemanager.util.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.util.TextUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public final class TaskQueueBuilder implements Runnable {
    private static final LockToggle TASK_LOCK = new LockToggle();

    private final LinkedList<QueueExecution> taskChain;
    private final ResourceBundle resources;
    private final HashSet<Runnable> succeededTasks;
    private final Logger logger;

    /**
     * Chain multiple {@link QueueTask} so they execute consecutively.
     * Once built, {@link #run} method can be called multiple times safely.
     *
     * @param resources The current i18n resource bundle
     */
    private TaskQueueBuilder(ResourceBundle resources) throws TaskInProgressException {
        if (TASK_LOCK.isLocked()) {
            throw new TaskInProgressException();
        }

        this.resources = resources;
        this.taskChain = new LinkedList<>();
        this.succeededTasks = new HashSet<>();
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static TaskQueueBuilder create(ResourceBundle resources) throws TaskInProgressException {
        return new TaskQueueBuilder(resources);
    }

    public <R> TaskQueueBuilder appendTask(QueueTask<R> nextTask) {
        return appendTask(nextTask, null, null);
    }

    public <R> TaskQueueBuilder appendTask(QueueTask<R> nextTask, Consumer<R> onTaskResult) {
        return appendTask(nextTask, onTaskResult, null);
    }

    public <R> TaskQueueBuilder appendTask(QueueTask<R> nextTask, Consumer<R> onTaskResult, Consumer<Throwable> onException) {
        taskChain.add(new QueueExecution<>(nextTask, onTaskResult, onException));
        return this;
    }

    public TaskQueueBuilder onSucceeded(Runnable handler) {
        this.succeededTasks.add(handler);
        return this;
    }

    @Override
    public void run() {
        TASK_LOCK.lock();

        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                for (QueueExecution execution : taskChain) {
                    final QueueTask task = execution.queueTask;
                    final String taskTitle = task.getTaskTitle(resources);
                    logger.log(Level.INFO, "Running task " + taskTitle);

                    if (!TextUtils.isEmpty(taskTitle)) {
                        Platform.runLater(() -> Main.getPrimaryController().activateProgressBar(task));
                    }

                    task.run();
                    Platform.runLater(() -> {
                        final Throwable exception = task.getException();
                        if (exception != null) {
                            logger.log(Level.SEVERE, "Error during task " + taskTitle, exception);
                            execution.emitException(exception);
                        } else {
                            final Object value = task.getValue();

                            if (value != null) {
                                if (value instanceof Collection) {
                                    final int size = ((Collection) value).size();
                                    if (size > 0) {
                                        logger.log(Level.INFO, "Task done, " + size + " values emitted");
                                    }
                                } else {
                                    logger.log(Level.INFO, "Task done, value emitted: " + value);
                                }
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

    private class QueueExecution<R> {
        private final QueueTask<R> queueTask;
        private final Consumer<R> onTaskResult;
        private Consumer<Throwable> onException;

        private QueueExecution(QueueTask<R> queueTask,
                               Consumer<R> onTaskResult,
                               Consumer<Throwable> onException) {
            this.queueTask = queueTask;
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
