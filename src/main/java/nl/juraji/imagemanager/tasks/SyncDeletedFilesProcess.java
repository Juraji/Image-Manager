package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 24-8-2018.
 * Image Manager
 */
public class SyncDeletedFilesProcess extends Process<Integer> {
    private final Directory directory;

    public SyncDeletedFilesProcess(Directory directory) {
        this.directory = directory;

        this.setTitle(TextUtils.format(resources, "tasks.syncDeletedFilesTask.title", directory.getName()));
    }

    @Override
    public Integer call() {
        this.checkValidity();

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
