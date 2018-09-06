package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ScanLocalDirectoryTask extends QueueTask<Void> {
    private static final String[] SUPPORTED_EXTENSIONS = new String[]{"jpg", "gif", "png", "bmp", "webp", "tiff"};
    private final Directory directory;
    private final Dao dao;

    public ScanLocalDirectoryTask(Directory directory) {
        this.directory = directory;
        this.dao = new Dao();
    }

    @Override
    public Void call() {
        this.checkValidity();

        final List<File> files = FileUtils.listFiles(this.directory.getTargetLocation(), true, SUPPORTED_EXTENSIONS);
        final List<ImageMetaData> existingData = Collections.unmodifiableList(this.directory.getImageMetaData());
        updateProgress(0, files.size());

        final Set<ImageMetaData> newMetaData = files.stream()
                .peek(f -> updateProgress())
                .filter(file -> existingData.stream()
                        .map(ImageMetaData::getFile)
                        .noneMatch(f -> f.equals(file)))
                .map(this::mapToMetaData)
                .collect(Collectors.toSet());

        if (newMetaData.size() > 0) {
            this.dao.save(newMetaData);
            this.directory.getImageMetaData().addAll(newMetaData);
        }

        return null;
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.scanLocalDirectoryTask.title", directory.getName());
    }

    private ImageMetaData mapToMetaData(File file) {
        final ImageMetaData data = new ImageMetaData();

        data.setDirectory(this.directory);
        data.setFile(file);
        data.setDateAdded(LocalDateTime.now());
        data.getTags().add("Local");

        return data;
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
