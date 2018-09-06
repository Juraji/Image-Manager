package nl.juraji.imagemanager.util.ui.modelfields.handlers;

import nl.juraji.imagemanager.util.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Juraji on 6-9-2018.
 * Image Manager
 */
public class ListTextFieldControlHandler extends TextFieldControlHandler {

    public ListTextFieldControlHandler(Object bean, String property, boolean isTextArea) {
        super(bean, property, isTextArea);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setControlValue(Object value) throws Exception {
        String fieldValue = ((List<String>) value).stream()
                .reduce((l, r) -> l + ", " + r)
                .orElse("");
        super.setControlValue(fieldValue);
    }

    @Override
    public void bindBeanProperty() {
        this.control.textProperty().addListener((obs, o, n) -> {
            final List<String> collect = Arrays.stream(n.split(","))
                    .map(TextUtils::trim)
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toList());
            this.setBeanProperty(collect);
        });
    }
}
