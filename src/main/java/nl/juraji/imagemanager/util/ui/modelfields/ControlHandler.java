package nl.juraji.imagemanager.util.ui.modelfields;

import javafx.scene.control.Control;
import org.apache.commons.beanutils.PropertyUtils;

import static nl.juraji.imagemanager.util.ExceptionUtils.catchAll;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public abstract class ControlHandler<T extends Control> {
    private final Object bean;
    private final String property;

    protected final T control;
    protected boolean nullable;

    protected ControlHandler(Object bean, String property, T control) {
        this.bean = bean;
        this.property = property;
        this.control = control;

        catchAll(() -> {
            final Object o = getBeanProperty();
            this.setControlValue(o);
            this.bindBeanProperty();
        });
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isDisabled() {
        return control.isDisabled();
    }

    public void setDisabled(boolean disabled) {
        control.setDisable(disabled);
    }

    public abstract boolean isFieldInvalid();

    public T getControl(){
        return control;
    }

    public abstract String getTextValue();

    protected Object getBeanProperty() {
        return catchAll(() -> PropertyUtils.getProperty(bean, property));
    }

    protected void setBeanProperty(Object o) {
        catchAll(() -> PropertyUtils.setProperty(bean, property, o));
    }

    protected abstract void setControlValue(Object value) throws Exception;

    protected abstract void bindBeanProperty() throws Exception;
}
