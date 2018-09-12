package nl.juraji.imagemanager.tasks;

import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;
import nl.juraji.imagemanager.tasks.refresh.ScanLocalDirectoryProcess;
import nl.juraji.imagemanager.tasks.refresh.ScanPinterestBoardProcess;
import nl.juraji.imagemanager.util.concurrent.Process;

/**
 * Created by Juraji on 21-8-2018.
 * Image Manager
 */
public final class DirectoryScanners {
    private DirectoryScanners() {
    }

    public static Process<Void> forDirectory(Directory directory) {
        final Class<? extends Directory> clazz = directory.getClass();
        if (Directory.class.equals(clazz)) {
            return new ScanLocalDirectoryProcess(directory);
        } else if (PinterestBoard.class.isAssignableFrom(clazz)) {
            return new ScanPinterestBoardProcess((PinterestBoard) directory);
        }

        throw new UnsupportedOperationException("Unsupported directory type: " + clazz.getName());
    }
}
