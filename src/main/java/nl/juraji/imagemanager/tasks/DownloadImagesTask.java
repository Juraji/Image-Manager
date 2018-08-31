package nl.juraji.imagemanager.tasks;

import com.google.common.base.Strings;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class DownloadImagesTask extends QueueTask<Void> {

    private final Directory directory;
    private final Dao dao;
    private final Logger logger;

    public DownloadImagesTask(Directory directory) {
        this.directory = directory;
        this.dao = new Dao();
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public String getTaskTitle(ResourceBundle resources) {
        return TextUtils.format(resources, "tasks.downloadImagesTask.title", directory.getName());
    }

    @Override
    protected Void call() throws Exception {
        if (!(directory instanceof PinterestBoard)) {
            // Check of Directory is Pinterest board, else this task can be skipped
            return null;
        }

        if (!directory.getTargetLocation().exists()) {
            Files.createDirectories(directory.getTargetLocation().toPath());
        }

        final List<PinMetaData> pinsToDownload = directory.getImageMetaData().stream()
                .filter(i -> i instanceof PinMetaData)
                .map(i -> (PinMetaData) i)
                .filter(p -> p.getDownloadUrls() != null && !p.getFile().exists())
                .collect(Collectors.toList());

        final int pinsToDownloadCount = pinsToDownload.size();
        updateProgress(0, pinsToDownloadCount);

        pinsToDownload.parallelStream()
                .peek(p -> updateProgress())
                .forEach(this::downloadPin);

        dao.save(pinsToDownload);
        return null;
    }

    private void downloadPin(PinMetaData pinMetaData) {
        final List<String> sortedImgUrls = pinMetaData.getDownloadUrls().entrySet().stream()
                .sorted((a, b) -> b.getKey() - a.getKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        boolean failed = true;

        for (String imgUrl : sortedImgUrls) {
            final String fileName = createTargetFileName(pinMetaData, imgUrl);

            try {
                final File download = doDownload(imgUrl, fileName);
                pinMetaData.setFile(download);
                failed = false;
                break;
            } catch (IOException ignored) {
                logger.log(Level.WARNING, "Failed downloading pin from " + imgUrl + ", trying next uri...");
            }
        }

        if (failed) {
            logger.log(Level.WARNING, "Failed downloading pin " + pinMetaData.getPinId() + ", giving up!");
        }
    }

    private String createTargetFileName(PinMetaData pin, String imgUrl) {
        String ext = imgUrl.substring(imgUrl.lastIndexOf("."));

        if (Strings.isNullOrEmpty(pin.getDescription())) {
            return pin.getPinId() + ext;
        } else {
            String description = TextUtils.cutOff(pin.getDescription(), 64, false);
            description = TextUtils.getFileSystemSafeName(description);
            return pin.getPinId() + " - " + description + ext;
        }
    }

    private File doDownload(String uri, String pinFileName) throws IOException {
        File targetFile = new File(directory.getTargetLocation(), pinFileName);

        // Only perform download if file doesn't already exist
        if (!targetFile.exists()) {
            try (InputStream input = new URL(uri).openStream()) {
                Files.copy(input, targetFile.toPath());
                return targetFile;
            }
        }

        return targetFile;
    }
}
