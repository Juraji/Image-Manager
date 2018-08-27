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
        if (PinterestBoard.class.isAssignableFrom(directory.getClass())) {
            return new ScanPinterestDirectoryTask(directory);
        } else {
            return new ScanLocalDirectoryTask(directory);
        }
    }
}
