package nl.juraji.imagemanager.components.builders;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import nl.juraji.imagemanager.model.pinterest.PinterestBoard;

import java.util.List;
import java.util.Optional;


/**
 * Created by Juraji on 28-8-2018.
 * Image Manager
 */
public class PinterestBoardChooserBuilder {
    private static final double PREF_WIDTH = 400.0;

    private final Dialog<List<PinterestBoard>> dialog;
    private final ListView<PinterestBoard> listView;

    public PinterestBoardChooserBuilder(Stage owner) {

        this.dialog = new Dialog<>();
        this.dialog.initOwner(owner);
        this.dialog.setResizable(true);
        this.dialog.getDialogPane().setMinWidth(PREF_WIDTH);
        this.dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        // Set buttons
        this.dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.APPLY, ButtonType.CANCEL);

        // Create list
        // Todo create selection list content
        this.listView = new ListView<>();
        this.listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listView.prefWidth(PREF_WIDTH);

        this.listView.setCellFactory(param -> new ListCell<PinterestBoard>() {
            @Override
            protected void updateItem(PinterestBoard item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    super.setText(item.getName() + " (" + item.getBoardUrl().toString() + ")");
                }
            }
        });

        this.dialog.getDialogPane().setContent(this.listView);

        this.dialog.setResultConverter(button -> {
            if (ButtonType.APPLY.equals(button)) {
                final ObservableList<PinterestBoard> selectedItems = this.listView.getSelectionModel().getSelectedItems();
                return selectedItems != null && selectedItems.size() > 0 ? selectedItems : null;
            }

            return null;
        });
    }

    public static PinterestBoardChooserBuilder create(Stage owner) {
        return new PinterestBoardChooserBuilder(owner);
    }

    public PinterestBoardChooserBuilder withTitle(String title) {
        this.dialog.setTitle(title);
        return this;
    }

    public PinterestBoardChooserBuilder withPinterestBoards(List<PinterestBoard> availableBoards) {
        this.listView.getItems().addAll(availableBoards);
        return this;
    }

    public Optional<List<PinterestBoard>> showAndWait() {
        return this.dialog.showAndWait();
    }
}
