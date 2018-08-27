package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.util.ResourceBundle;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ScanPinterestDirectoryTask extends QueueTask<Void> {

    private final Directory directory;

    public ScanPinterestDirectoryTask(Directory directory) {
        this.directory = directory;
    }

    @Override
    public Void call() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.scanPinterestDirectoryTask.title", directory.getName());
    }
}
