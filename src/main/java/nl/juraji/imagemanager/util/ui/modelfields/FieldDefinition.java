package nl.juraji.imagemanager.util.ui.modelfields;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class FieldDefinition {
    private final ControlHandler control;
    private final String i18nLabelKey;

    public FieldDefinition(ControlHandler control, String i18nLabelKey) {
        this.control = control;
        this.i18nLabelKey = i18nLabelKey;
    }

    public String getI18nLabelKey() {
        return i18nLabelKey;
    }

    public ControlHandler getHandler() {
        return control;
    }
}
