package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.util.concurrent.QueueTask;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public final class DirectoryScanners {
    private DirectoryScanners() {
    }

    public static QueueTask<Void> forDirectory(Directory directory) {
        final Class<? extends Directory> clazz = directory.getClass();
        if (Directory.class.equals(clazz)) {
            return new ScanLocalDirectoryTask(directory);
        } else if (PinterestBoard.class.isAssignableFrom(clazz)) {
            return new ScanPinterestBoardTask((PinterestBoard) directory);
        }

        throw new UnsupportedOperationException("Unsupported directory type: " + clazz.getName());
    }
}
