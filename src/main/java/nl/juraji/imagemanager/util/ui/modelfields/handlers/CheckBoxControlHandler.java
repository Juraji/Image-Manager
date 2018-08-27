package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import javafx.scene.control.CheckBox;
import nl.juraji.imagemanager.util.ui.modelfields.ControlHandler;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class CheckBoxControlHandler extends ControlHandler<CheckBox> {

    public CheckBoxControlHandler(Object bean, String property, boolean nullable) {
        super(bean, property, new CheckBox(), nullable);
    }

    @Override
    public boolean isFieldInvalid() {
        return false;
    }

    @Override
    public String getTextValue() {
        return String.valueOf(this.control.isSelected());
    }

    @Override
    protected void setControlValue(Object value) {
        this.control.setSelected(value != null && (boolean) value);
    }

    @Override
    public void bindBeanProperty() {
        this.control.selectedProperty().addListener((obs, o, n) -> this.setBeanProperty(n));
    }
}
