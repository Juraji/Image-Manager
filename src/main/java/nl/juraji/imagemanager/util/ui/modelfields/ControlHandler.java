package nl.juraji.imagemanager.util.ui.modelfields;

import javafx.scene.control.Control;
import org.apache.commons.beanutils.PropertyUtils;

import static nl.juraji.imagemanager.util.ExceptionUtils.catchAll;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public abstract class ControlHandler<T extends Control> {
    protected final Object bean;
    protected final String property;
    protected final T control;
    protected final boolean nullable;

    public ControlHandler(Object bean, String property, T control, boolean nullable) {
        this.bean = bean;
        this.property = property;
        this.control = control;
        this.nullable = nullable;

        catchAll(() -> {
            final Object o = PropertyUtils.getProperty(bean, property);
            this.setControlValue(o);
            this.bindBeanProperty();
        });
    }

    public abstract boolean isFieldInvalid();

    public T getControl(){
        return control;
    }

    public abstract String getTextValue();

    protected void setBeanProperty(Object o) {
        catchAll(() -> PropertyUtils.setProperty(bean, property, o));
    }

    protected abstract void setControlValue(Object value) throws Exception;

    protected abstract void bindBeanProperty() throws Exception;
}
