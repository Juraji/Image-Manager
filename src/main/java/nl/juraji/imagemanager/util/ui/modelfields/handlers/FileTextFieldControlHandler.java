package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import java.io.File;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class FileTextFieldControlHandler extends TextFieldControlHandler {
    public FileTextFieldControlHandler(Object bean, String property, boolean nullable) {
        super(bean, property, nullable);
    }

    @Override
    public boolean isFieldInvalid() {
        return super.isFieldInvalid() || !new File(this.control.getText()).isDirectory();
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
