package nl.juraji.imagemanager.ui.builders;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import nl.juraji.imagemanager.util.TextUtils;

import java.io.File;
import java.util.function.Consumer;

/**
 * Created by Juraji on 19-8-2018.
 * Image Manager
 */
public final class DirectoryChooserBuilder {
    private final DirectoryChooser chooser;
    private final Window owner;

    private DirectoryChooserBuilder(Window owner, File root) {
        this.owner = owner;
        this.chooser = new DirectoryChooser();
        this.chooser.setInitialDirectory(root);
    }

    public static DirectoryChooserBuilder create(Window owner) {
        return new DirectoryChooserBuilder(owner, new File(System.getProperty("user.home")));
    }

    public static DirectoryChooserBuilder create(Window owner, File root) {
        return new DirectoryChooserBuilder(owner, root);
    }

    public DirectoryChooserBuilder withTitle(String title, Object... params) {
        this.chooser.setTitle(TextUtils.format(title, params));
        return this;
    }

    public void show(Consumer<File> onDirectorySelected) {
        final File file = this.chooser.showDialog(owner);

        if (file != null) {
            onDirectorySelected.accept(file);
        }
    }
}
