package nl.juraji.imagemanager.util.ui.events;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Created by Juraji on 6-9-2018.
 * Image Manager
 */
public class DefaultMouseEvent extends MouseEvent {
    public DefaultMouseEvent() {
        super(MouseEvent.ANY, 0.0, 0.0, 0.0, 0.0, MouseButton.NONE, 0,
                false, false, false, false,
                false, false, false, true,
                false, false, null);
    }
}
