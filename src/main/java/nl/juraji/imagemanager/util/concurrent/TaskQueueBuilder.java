package nl.juraji.imagemanager.util.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Window;
import nl.juraji.imagemanager.Main;
import nl.juraji.imagemanager.dialogs.ProgressDialog;
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
    private final LinkedList<QueueExecution> taskChain;
    private final ProgressDialog progressDialog;
    private final ResourceBundle resources;
    private final HashSet<Runnable> succeededTasks;
    private final Logger logger;

    /**
     * Chain multiple {@link QueueTask} so they execute consecutively.
     * Once built, {@link #run} method can be called multiple times safely.
     *
     * @param resources The current i18n resource bundle
     */
    private TaskQueueBuilder(Window owner, ResourceBundle resources) {
        this.resources = resources;
        this.taskChain = new LinkedList<>();
        this.succeededTasks = new HashSet<>();
        this.progressDialog = new ProgressDialog(owner, resources.getString("tasks.taskQueueBuilder.progressDialog.title"));
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static TaskQueueBuilder create(ResourceBundle resources) {
        return new TaskQueueBuilder(Main.getPrimaryStage(), resources);
    }

    public static TaskQueueBuilder create(Window owner, ResourceBundle resources) {
        return new TaskQueueBuilder(owner, resources);
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
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                for (QueueExecution execution : taskChain) {
                    final QueueTask task = execution.queueTask;
                    final String taskTitle = task.getTaskTitle(resources);
                    logger.log(Level.INFO, "Running task " + taskTitle);

                    if (!TextUtils.isEmpty(taskTitle)) {
                        Platform.runLater(() -> progressDialog.activateProgressBar(task, taskTitle));
                    }

                    task.run();
                    Platform.runLater(() -> {
                        final Throwable exception = task.getException();
                        if (exception != null) {
                            execution.emitException(exception);
                            progressDialog.close();
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
            progressDialog.close();
            //noinspection unchecked
            succeededTasks.forEach(Runnable::run);
        });

        new Thread(task).start();
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
