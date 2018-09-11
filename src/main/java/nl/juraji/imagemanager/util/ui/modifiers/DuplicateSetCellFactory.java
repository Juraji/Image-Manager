package nl.juraji.imagemanager.util.ui.modifiers;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import nl.juraji.imagemanager.tasks.DuplicateScanProcess.DuplicateSet;
import nl.juraji.imagemanager.util.TextUtils;

/**
 * Created by Juraji on 27-8-2018.
 * Image Manager
 */
public class DuplicateSetCellFactory implements Callback<ListView<DuplicateSet>, ListCell<DuplicateSet>> {

    private static final int FILE_NAME_MAX_LEN = 30;

    @Override
    public ListCell<DuplicateSet> call(ListView<DuplicateSet> listView) {
        return new ListCell<DuplicateSet>() {
            @Override
            protected void updateItem(DuplicateSet item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(TextUtils.format("{} -> {} ({})",
                            item.getDirectory().getName(),
                            TextUtils.cutOff(item.getParent().getFile().getName(), FILE_NAME_MAX_LEN),
                            item.getImageMetaData().size()));
                }
            }
        };
    }
}
