package nl.juraji.imagemanager.tasks;

import net.sf.jmimemagic.*;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class CorrectImageTypesTask extends QueueTask<Void> {

    private final Directory directory;
    private final Dao dao;

    public CorrectImageTypesTask(Directory directory) {
        this.directory = directory;
        this.dao = new Dao();
    }

    @Override
    public Void call() {
        final List<ImageMetaData> list = directory.getImageMetaData();

        for (ImageMetaData metaData : list) {
            final File file = metaData.getFile();

            try {
                // Skip empty meta data
                if (file != null) {
                    final File matchedFile = getMagicMatchedFile(file);

                    // #getMagicMatchedFile will return a new File object when the file should be moved
                    if (!file.equals(matchedFile)) {
                        try {
                            // Move the original file to the matchedFile location
                            Files.move(file.toPath(), matchedFile.toPath());
                        } catch (FileAlreadyExistsException e) {
                            // The target file already exists, so delete the newly downloaded one and proceed
                            Files.deleteIfExists(file.toPath());
                        }

                        metaData.setFile(file);
                        dao.save(metaData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            incrementProgress(list.size());
        }

        return null;
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.correctImageTypesTask.title", directory.getName());
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
