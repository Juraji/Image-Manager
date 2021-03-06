package nl.juraji.imagemanager.tasks.refresh;

import net.sf.jmimemagic.*;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class CorrectImageTypesProcess extends Process<Void> {

    private final Directory directory;
    private final Dao dao;

    public CorrectImageTypesProcess(Directory directory) {
        super();
        this.directory = directory;
        this.dao = new Dao();

        this.setTitle(TextUtils.format(resources, "tasks.correctImageTypesTask.title", directory.getName()));
    }

    @Override
    public Void call() {
        this.handleDirectoryRecursive(directory);
        return null;
    }

    private void handleDirectoryRecursive(Directory directory) {
        if (directory.isIgnored()) {
            // Do not handle ignored directories
            return;
        }

        final List<ImageMetaData> list = directory.getImageMetaData();
        addToMaxProgress(list.size());

        for (ImageMetaData metaData : list) {
            final File file = metaData.getFile();

            try {
                // Skip empty meta data
                if (file != null) {
                    final File matchedFile = getMagicMatchedFile(file);

                    // #getMagicMatchedFile will return a new File object when the file should be moved
                    if (!file.equals(matchedFile)) {
                        if (matchedFile.exists()) {
                            // Target already exists, remove the newly downloaded one
                            Files.deleteIfExists(file.toPath());
                        } else {
                            // Move the original file to the matchedFile location
                            Files.move(file.toPath(), matchedFile.toPath());
                        }

                        metaData.setFile(file);
                        dao.save(metaData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateProgress();
        }

        // Handle child directories
        if (directory.getDirectories().size() > 0) {
            for (Directory childDirectory : directory.getDirectories()) {
                this.handleDirectoryRecursive(childDirectory);
            }
        }
    }

    /**
     * Checks image file type using JMimeMagic
     *
     * @param file The File to match
     * @return A new File instance with the correct file extension
     */
    private File getMagicMatchedFile(File file) {
        try {
            final String orgFilePath = file.getAbsolutePath();
            final String orgExtension = orgFilePath.substring(orgFilePath.lastIndexOf('.') + 1);
            final MagicMatch magicMatch = Magic.getMagicMatch(file, false, true);

            final String magicMatchExtension = magicMatch.getExtension();
            if (!TextUtils.isEmpty(magicMatchExtension)) {
                if (!magicMatchExtension.equals(orgExtension)) {
                    file = new File(orgFilePath.replace(orgExtension, magicMatchExtension));
                }
            }
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            // If file type can't be inferred there's nothing we can do
        }

        return file;
    }
}
