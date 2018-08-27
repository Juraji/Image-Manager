package nl.juraji.imagemanager.util.ui.cellfactories;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import nl.juraji.imagemanager.model.Directory;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class DirectoryFavoriteCellFactory<S, T> implements Callback<TableColumn<Directory, Boolean>, TableCell<Directory, Boolean>> {
    private static final int IMAGE_SIZE = 14;
    private static final String IMAGE_RESOURCE = "/nl/juraji/imagemanager/images/favorite.png";

    private final ImageView imageView;

    public DirectoryFavoriteCellFactory() {
        imageView = new ImageView(new Image(IMAGE_RESOURCE, IMAGE_SIZE, IMAGE_SIZE, true, true, true));
    }

    @Override
    public TableCell<Directory, Boolean> call(TableColumn<Directory, Boolean> column) {
        return new TableCell<Directory, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);

                if (empty || !item) {
                    setGraphic(null);
                } else {
                    setGraphic(imageView);
                }
            }
        };
    }
}
