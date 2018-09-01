package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 24-8-2018.
 * Image Manager
 */
public class SyncDeletedFilesTask extends QueueTask<Integer> {
    private final Directory directory;

    public SyncDeletedFilesTask(Directory directory) {
        this.directory = directory;
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.syncDeletedFilesTask.title", directory.getName());
    }

    @Override
    protected Integer call() {
        this.checkValidity();

        new Dao().load(directory, "imageMetaData");

        final List<ImageMetaData> deletedMetaData = directory.getImageMetaData().stream()
                .filter(imageMetaData -> !imageMetaData.getFile().exists())
                .collect(Collectors.toList());

        if (deletedMetaData.size() > 0) {
            directory.getImageMetaData().removeAll(deletedMetaData);
            new Dao().delete(deletedMetaData);
        }

        return deletedMetaData.size();
    }

    private void checkValidity() {
        if (this.directory == null) {
            throw new RuntimeException("Directory may not be null");
        }

        if (this.directory.getTargetLocation() == null) {
            throw new RuntimeException("Target location is null for " + directory.getName());
        }
    }
}
