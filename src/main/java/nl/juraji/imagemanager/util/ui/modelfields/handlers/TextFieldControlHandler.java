package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import javafx.scene.control.TextField;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.modelfields.ControlHandler;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class TextFieldControlHandler extends ControlHandler<TextField> {
    public TextFieldControlHandler(Object bean, String property, boolean nullable) {
        super(bean, property, new TextField(),  nullable);
    }

    @Override
    public boolean isFieldInvalid() {
        return !nullable && TextUtils.isEmpty(this.control.getText());
    }

    @Override
    public String getTextValue() {
        return this.control.getText();
    }

    @Override
    protected void setControlValue(Object value) throws Exception {
        this.control.setText(String.valueOf(value));
    }

    @Override
    public void bindBeanProperty() {
        this.control.textProperty().addListener((obs, o, n) -> this.setBeanProperty(n));
    }
}
