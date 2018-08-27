package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import java.net.URI;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class URITextFieldControlHandler extends TextFieldControlHandler {
    public URITextFieldControlHandler(Object bean, String property, boolean nullable) {
        super(bean, property, nullable);
    }

    @Override
    public boolean isFieldInvalid() {
        try {
            if(!super.isFieldInvalid()) {
                // Simply check if URI can be created
                //noinspection ResultOfMethodCallIgnored
                URI.create(this.control.getText());
            }

            return false;
        } catch (IllegalArgumentException ignored) {
        }

        return true;
    }

    @Override
    public void bindBeanProperty() {
        this.control.textProperty().addListener((obs, o, n) -> {
            final URI uri = URI.create(n);
            this.setBeanProperty(uri);
        });
    }
}
