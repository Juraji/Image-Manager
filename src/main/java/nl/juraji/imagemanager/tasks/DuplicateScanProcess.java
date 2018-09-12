package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.ImageHash;
import nl.juraji.imagemanager.model.ImageMetaData;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.concurrent.Process;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 27-8-2018.
 * Image Manager
 */
public class DuplicateScanProcess extends Process<List<DuplicateScanProcess.DuplicateSet>> {

    private static final int CARDINALITY_THRESHOLD = 1000; // of ~9900
    private final Directory directory;

    public DuplicateScanProcess(Directory directory) {
        this.directory = directory;

        setTitle(TextUtils.format(resources, "tasks.duplicateScanTask.title", directory.getName()));
    }

    @Override
    public List<DuplicateSet> call() {
        final List<ImageMetaData> metaData = directory.getImageMetaData();

        final ArrayList<ImageMetaData> compareQueue = new ArrayList<>(metaData);
        setMaxProgress(metaData.size());

        return metaData.stream()
                .peek(i -> this.updateProgress())
                .map(parent -> {
                    final List<ImageMetaData> collect = compareQueue.stream()
                            .filter(child -> !child.equals(parent))
                            .filter(child -> this.compareHashes(parent, child))
                            .collect(Collectors.toList());

                    if (collect.size() > 0) {
                        compareQueue.remove(parent);
                        compareQueue.removeAll(collect);
                        collect.add(parent);
                        return new DuplicateSet(directory, parent, collect);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean compareHashes(ImageMetaData parent, ImageMetaData child) {
        final ImageHash ph = parent.getImageHash();
        final ImageHash ch = child.getImageHash();

        if (ph != null && ch != null) {
            if (Objects.equals(ph.getContrast(), ch.getContrast())) {
                BitSet xor = (BitSet) ph.getHash().clone();
                xor.xor(ch.getHash());
                return xor.cardinality() < CARDINALITY_THRESHOLD;
            }
        }
        return false;
    }

    public class DuplicateSet {
        private final Directory directory;
        private ImageMetaData parent;
        private List<ImageMetaData> imageMetaData;

        public DuplicateSet(Directory directory, ImageMetaData parent, List<ImageMetaData> imageMetaData) {
            this.directory = directory;
            this.parent = parent;
            this.imageMetaData = imageMetaData;
        }

        public Directory getDirectory() {
            return directory;
        }

        public ImageMetaData getParent() {
            return parent;
        }

        public List<ImageMetaData> getImageMetaData() {
            return imageMetaData;
        }
    }

    public enum ScanType {
        PER_DIRECTORY_SCAN, FULL_SCAN
    }
}
