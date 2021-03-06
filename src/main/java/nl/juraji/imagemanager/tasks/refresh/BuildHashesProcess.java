package nl.juraji.imagemanager.tasks.refresh;

import nl.juraji.imagemanager.model.*;
import nl.juraji.imagemanager.util.Log;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import static java.awt.RenderingHints.*;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public class BuildHashesProcess extends Process<Void> {
    private static final int SAMPLE_SIZE = 100;

    private final Logger logger;
    private final Directory directory;
    private final Dao dao;

    public BuildHashesProcess(Directory directory) {
        super();
        this.directory = directory;
        this.dao = new Dao();
        this.logger = Log.create(this);

        this.setTitle(TextUtils.format(resources, "tasks.buildHashesTask.title", directory.getName()));
    }

    @Override
    public Void call() {
        this.checkValidity();

        this.handleDirectoryRecursive(this.directory);
        return null;
    }

    private void handleDirectoryRecursive(Directory directory) {
        if(directory.isIgnored()) {
            // Do not handle ignored directories
            return;
        }

        final List<ImageMetaData> list = this.directory.getImageMetaData();
        addToMaxProgress(list.size());

        list.parallelStream()
                .peek(i -> updateProgress())
                .filter(i -> i.getImageHash() == null)
                .forEach(this::generate);

        // Persist changes
        dao.save(this.directory.getImageMetaData());

        if(directory.getDirectories().size()>0){
            for (Directory childDirectory : directory.getDirectories()) {
                this.handleDirectoryRecursive(childDirectory);
            }
        }
    }

    private void generate(ImageMetaData imageMetaData) {
        try {
            final BufferedImage image = ImageIO.read(imageMetaData.getFile());

            if (image == null) {
                throw new IOException("Failed reading file");
            }

            final ImageHash hash = new ImageHash();
            final long qualityRating = calculateQualityRating(image, imageMetaData.getFile());

            generateHash(image, hash);

            hash.setImageMetaData(imageMetaData);
            imageMetaData.setQualityRating(qualityRating);
            imageMetaData.setImageWidth(image.getWidth());
            imageMetaData.setImageHeight(image.getHeight());
            imageMetaData.setFileSize(imageMetaData.getFile().length());
            imageMetaData.setImageHash(hash);
        } catch (Throwable e) {
            logger.warn("Build hash failed for " + imageMetaData.getFile().getAbsolutePath(), e);
        }
    }

    private long calculateQualityRating(BufferedImage image, File originalFile) {
        return (image.getWidth() * image.getHeight()) + originalFile.length();
    }

    /**
     * Generates a bitset hash and Contrast value and sets it hash object
     *
     * @param image The image to hash (Warn: Buffer gets modified)
     * @param hash  The PinImageHash object to update
     */
    private void generateHash(BufferedImage image, ImageHash hash) {
        // Scale the image to SAMPE_SIZE by SAMPLE_SIZE in grayscale and draw it back on the image buffer (at 0,0)
        final Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE, null);
        graphics.dispose();

        // The last column is ignored due to it
        // not having a next column for comparison
        final int scanXCount = SAMPLE_SIZE - 1;
        final int scanYCount = SAMPLE_SIZE;
        final int totalXY = scanXCount * scanYCount;
        final BitSet set = new BitSet(totalXY);
        long totalRGB = 0;
        int iter = 0;

        for (int y = 0; y < scanYCount; y++) {
            for (int x = 0; x < scanXCount; x++) {
                int rgbA = image.getRGB(x, y) & 255;
                int rgbB = image.getRGB(x + 1, y) & 255;
                set.set(iter, rgbA < rgbB);
                totalRGB += rgbA;
                ++iter;
            }
        }

        hash.setContrast(Contrast.forRGB((int) (totalRGB / totalXY)));
        hash.setHash(set);
    }

    private void checkValidity() {
        if (this.directory == null) {
            throw new RuntimeException("Directory may not be null");
        }
    }
}
