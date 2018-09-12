package nl.juraji.imagemanager.tasks;

import javafx.application.Platform;
import nl.juraji.imagemanager.model.Directory;
import nl.juraji.imagemanager.tasks.refresh.BuildHashesProcess;
import nl.juraji.imagemanager.tasks.refresh.CorrectImageTypesProcess;
import nl.juraji.imagemanager.tasks.refresh.DownloadImagesProcess;
import nl.juraji.imagemanager.util.concurrent.Process;

/**
 * Created by Juraji on 12-9-2018.
 * Image Manager
 */
public class RefreshDirectoryProcess extends Process<Void> {
    private final Directory directory;

    public RefreshDirectoryProcess(Directory directory) {
        this.directory = directory;
    }

    @Override
    public Void call() throws Exception {

        // Step 1: Scan directory
        final Process<Void> directoryScanner = DirectoryScanners.forDirectory(directory);
        runAndAwaitSubProcess(directoryScanner);

        // Step 2: Download images
        final DownloadImagesProcess downloadImagesProcess = new DownloadImagesProcess(directory);
        runAndAwaitSubProcess(downloadImagesProcess);

        // Step 3: Correct image file types
        final CorrectImageTypesProcess correctImageTypesProcess = new CorrectImageTypesProcess(directory);
        runAndAwaitSubProcess(correctImageTypesProcess);

        // Step 4: Build hashes
        final BuildHashesProcess buildHashesProcess = new BuildHashesProcess(directory);
        runAndAwaitSubProcess(buildHashesProcess);

        return null;
    }

    private void runAndAwaitSubProcess(Process<?> process) throws Exception {
        Platform.runLater(() -> {
            this.progressProperty().unbind();
            this.progressProperty().set(-1);
            this.progressProperty().bind(process.progressProperty());
            this.titleProperty().unbind();
            this.titleProperty().set(null);
            this.titleProperty().bind(process.titleProperty());
        });

        process.call();
    }
}
