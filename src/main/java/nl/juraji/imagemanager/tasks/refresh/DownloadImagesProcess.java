package nl.juraji.imagemanager.tasks.refresh;

import com.google.common.base.Strings;
import nl.juraji.imagemanager.model.Dao;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinMetaData;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class DownloadImagesProcess extends Process<Void> {

    private final Directory directory;
    private final Dao dao;
    private final Logger logger;

    public DownloadImagesProcess(Directory directory) {
        super();
        this.directory = directory;
        this.dao = new Dao();
        this.logger = Log.create(this);

        this.setTitle(TextUtils.format(resources, "tasks.downloadImagesTask.title", directory.getName()));
    }

    @Override
    public Void call() throws Exception {
        if (!(directory instanceof PinterestBoard)) {
            // Check of Directory is Pinterest board, else this task can be skipped
            return null;
        }

        this.handleBoardRecursive((PinterestBoard) directory);
        return null;
    }

    private void handleBoardRecursive(PinterestBoard board) throws IOException {
        if (board.isIgnored()) {
            // Do not handle ignored boards
            return;
        }

        if (!directory.getTargetLocation().exists()) {
            Files.createDirectories(directory.getTargetLocation().toPath());
        }

        // Filter out already existing pins to download
        final List<PinMetaData> pinsToDownload = directory.getImageMetaData().stream()
                .filter(i -> i instanceof PinMetaData)
                .map(i -> (PinMetaData) i)
                .filter(p -> p.getDownloadUrl() != null && !p.getFile().exists())
                .collect(Collectors.toList());

        addToMaxProgress(pinsToDownload.size());

        pinsToDownload.parallelStream()
                .peek(p -> updateProgress())
                .forEach(this::downloadPin);

        // Handle child boards
        if (board.getDirectories().size() > 0) {
            for (Directory childBoard : board.getDirectories()) {
                this.handleBoardRecursive((PinterestBoard) childBoard);
            }
        }

        // Persist changes
        dao.save(pinsToDownload);
    }

    private void downloadPin(PinMetaData pinMetaData) {
        final String imgUrl = pinMetaData.getDownloadUrl().toString();
        final String fileName = createTargetFileName(pinMetaData, imgUrl);

        try {
            final File download = doDownload(imgUrl, fileName);
            pinMetaData.setFile(download);
        } catch (IOException ignored) {
            logger.warn("Failed downloading pin from " + imgUrl);
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
