package nl.juraji.imagemanager.util.ui.modelfields;

import javafx.scene.control.Control;
import nl.juraji.imagemanager.util.Log;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.util.Collection;

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

    /**
     * Get the internal control
     * @return The control assigned to this handler
     */
    public T getControl(){
        return control;
    }

    /**
     * Get the property value
     * @return The property value for the bean assigned to this handler
     */
    protected Object getBeanProperty() {
        return catchAll(() -> PropertyUtils.getProperty(bean, property));
    }

    /**
     * Update the bean property with a new value
     * @param value The value to assign
     */
    @SuppressWarnings("unchecked")
    protected void setBeanProperty(Object value) {
        catchAll(() -> {
            final PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(bean, property);
            if(Collection.class.isAssignableFrom(descriptor.getPropertyType())){
                final Collection<Object> collection = (Collection<Object>) PropertyUtils.getProperty(bean, property);
                collection.clear();
                collection.addAll((Collection<Object>) value);
            } else {
                PropertyUtils.setProperty(bean, property, value);
            }
        });
    }

    /**
     * Implement in subclass
     * Get the field validation state (Is null, Invalid format)
     * @return True when the field value is invalid
     */
    public abstract boolean isFieldInvalid();

    /**
     * Implement in subclass
     * Get the {@link String} representation of the field value
     * @return
     */
    public abstract String getTextValue();

    /**
     * Implement in subclass
     * Set the control value, based on the bean property
     * @param value The bean property value
     * @throws Exception On error
     */
    protected abstract void setControlValue(Object value) throws Exception;

    /**
     * Implement in subclass
     * Bind the fields output to the bean property
     * Use {@link #setBeanProperty(Object)} to update the bean
     * @throws Exception On Error
     */
    protected abstract void bindBeanProperty() throws Exception;
}
