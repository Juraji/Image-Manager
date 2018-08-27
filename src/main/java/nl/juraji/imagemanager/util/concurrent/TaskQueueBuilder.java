package nl.juraji.imagemanager.util.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Task;
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
    private TaskQueueBuilder(ResourceBundle resources) {
        this.resources = resources;
        taskChain = new LinkedList<>();
        succeededTasks = new HashSet<>();
        progressDialog = new ProgressDialog(resources.getString("tasks.taskQueueBuilder.progressDialog.title"));
        logger = Logger.getLogger(getClass().getName());
    }

    public static TaskQueueBuilder create(ResourceBundle resources) {
        return new TaskQueueBuilder(resources);
    }

    public <R> TaskQueueBuilder appendTask(QueueTask<R> nextTask) {
        return appendTask(nextTask, null);
    }

    public <R> TaskQueueBuilder appendTask(QueueTask<R> nextTask, Consumer<R> onIntermediateResult) {
        taskChain.add(new QueueExecution<>(nextTask, onIntermediateResult));
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
                        final Object value = task.getValue();
                        if (value != null) {
                            if (value instanceof Collection) {
                                final int size = ((Collection) value).size();
                                if (size > 0) {{
                                    //noinspection unchecked
                                    execution.emitIntermediateResult(value);
                                    logger.log(Level.INFO, "Task done, " + size + " values emitted");
                                }}
                            } else {
                                //noinspection unchecked
                                execution.emitIntermediateResult(value);
                                logger.log(Level.INFO, "Task done, value emitted: " + value);
                            }
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
        private final Consumer<R> onIntermediateResult;

        private QueueExecution(QueueTask<R> queueTask, Consumer<R> onIntermediateResult) {
            this.queueTask = queueTask;
            this.onIntermediateResult = onIntermediateResult;
        }

        public void emitIntermediateResult(R value) {
            if (this.onIntermediateResult != null) {
                this.onIntermediateResult.accept(value);
            }
        }
    }
}
