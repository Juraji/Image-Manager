package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import java.io.File;
import java.io.IOException;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class FileTextFieldControlHandler extends TextFieldControlHandler {
    public FileTextFieldControlHandler(Object bean, String property) {
        super(bean, property, false);
    }

    @Override
    public boolean isFieldInvalid() {
        try {
            if (super.isFieldInvalid()) {
                //noinspection ResultOfMethodCallIgnored
                new File(this.control.getText()).getCanonicalPath();
                return true;
            }
        } catch (IOException ignored) {
        }

        return false;
    }

    @Override
    protected void setControlValue(Object value) throws Exception {
        super.setControlValue(((File) value).getCanonicalPath());
    }

    @Override
    public void bindBeanProperty() {
        this.control.textProperty().addListener((obs, o, n) -> {
            final File file = new File(n);
            this.setBeanProperty(file);
        });
    }
}
