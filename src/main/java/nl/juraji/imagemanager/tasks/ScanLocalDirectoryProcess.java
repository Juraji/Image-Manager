package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.FileUtils;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class ScanLocalDirectoryProcess extends Process<Void> {
    private static final String[] SUPPORTED_EXTENSIONS = new String[]{"jpg", "gif", "png", "bmp", "webp", "tiff"};
    private final Directory directory;
    private final Dao dao;

    public ScanLocalDirectoryProcess(Directory directory) {
        this.directory = directory;
        this.dao = new Dao();
    }

    @Override
    public Void call() {
        this.checkValidity();

        final List<File> rootFiles = FileUtils.listFilesAndDirectories(this.directory.getTargetLocation(), SUPPORTED_EXTENSIONS);

        this.mapAndSaveFiles(this.directory, rootFiles);
        return null;
    }

    private void mapAndSaveFiles(Directory parent, List<File> files) {
        if(parent.isIgnored()) {
            // Do not map ignored directories
            return;
        }

        // Add count to progress max
        this.addToMaxProgress(files.size());

        final List<Directory> existingDirectories = parent.getDirectories();
        final List<ImageMetaData> existingMataData = parent.getImageMetaData();

        for (File file : files) {
            if (file.isDirectory()) {
                // Map and scan directory
                Directory child = existingDirectories.stream()
                        .filter(d -> d.getTargetLocation().equals(file))
                        .findFirst()
                        .orElse(null);

                // Create and persist if non-existent
                if (child == null) {
                    child = new Directory();
                    child.setName(file.getName());
                    child.setTargetLocation(file);
                    child.setParent(parent);
                    dao.save(child);

                    parent.getDirectories().add(child);
                }

                final List<File> childFiles = FileUtils.listFilesAndDirectories(file, SUPPORTED_EXTENSIONS);
                this.mapAndSaveFiles(child, childFiles);
            } else {
                // map metadata
                final boolean nonExistent = existingMataData.stream()
                        .map(ImageMetaData::getFile)
                        .noneMatch(file1 -> file1.equals(file));

                // Create and persist if non-existent
                if (nonExistent) {
                    final ImageMetaData data = new ImageMetaData();

                    data.setDirectory(parent);
                    data.setFile(file);
                    data.setDateAdded(LocalDateTime.now());
                    data.getTags().add(parent.getSourceType());

                    dao.save(data);
                    parent.getImageMetaData().add(data);
                }
            }

            // increment current progress
            updateProgress();
        }
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.scanLocalDirectoryTask.title", directory.getName());
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
