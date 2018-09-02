package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import nl.juraji.imagemanager.util.TextUtils;
import nl.juraji.imagemanager.util.ui.modelfields.ControlHandler;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class TextFieldControlHandler extends ControlHandler<TextInputControl> {
    public TextFieldControlHandler(Object bean, String property, boolean isTextArea) {
        super(bean, property, isTextArea ? new TextArea() : new TextField());

        // Resize text area, since default is way too small
        if (isTextArea) {
            final TextArea textArea = (TextArea) this.control;
            textArea.setPrefRowCount(3);
            textArea.setWrapText(true);
        }
    }

    @Override
    public boolean isFieldInvalid() {
        return !this.nullable && TextUtils.isEmpty(this.control.getText());
    }

    @Override
    public String getTextValue() {
        return this.control.getText();
    }

    @Override
    protected void setControlValue(Object value) throws Exception {
        if (value == null) {
            this.control.setText("");
        } else {
            this.control.setText(String.valueOf(value));
        }
    }

    @Override
    public void bindBeanProperty() {
        this.control.textProperty().addListener((obs, o, n) -> this.setBeanProperty(n));
    }
}
