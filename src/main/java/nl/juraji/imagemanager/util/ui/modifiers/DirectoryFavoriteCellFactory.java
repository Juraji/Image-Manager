package nl.juraji.imagemanager.util.ui.modifiers;

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
public class DirectoryFavoriteCellFactory<S extends Directory, T extends Boolean> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    private static final int IMAGE_SIZE = 14;
    private static final String IMAGE_RESOURCE = "/nl/juraji/imagemanager/images/favorite.png";

    private final Image image;

    public DirectoryFavoriteCellFactory() {
        image = new Image(IMAGE_RESOURCE, IMAGE_SIZE, IMAGE_SIZE, true, true, true);
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> column) {
        return new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);

                if (empty || !item.booleanValue()) {
                    setGraphic(null);
                } else {
                    setGraphic(new ImageView(image));
                }
            }
        };
    }
}
