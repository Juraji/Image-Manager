package nl.juraji.imagemanager.util.ui.modelfields;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public class FieldDefinition {
    private final ControlHandler control;
    private final String i18nKey;

    public FieldDefinition(ControlHandler control, String i18nKey) {
        this.control = control;
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public ControlHandler getHandler() {
        return control;
    }
}
