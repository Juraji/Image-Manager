package nl.juraji.imagemanager.util;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Created by Juraji on 10-9-2018.
 * Image Manager
 */
public class ModelUtils<T> {

    private final T entity;

    public ModelUtils(T entity) {
        this.entity = entity;
    }

    /**
     * Copy properties from another bean
     * This action overwrites current data
     *
     * @param from The source bean
     * @throws InvocationTargetException By {@link PropertyUtils#getProperty(Object, String)}
     */
    @SuppressWarnings("unchecked")
    public void copyPropertiesFrom(T from) throws InvocationTargetException {
        final PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(entity);

        for (PropertyDescriptor descriptor : descriptors) {
            final Method readMethod = descriptor.getReadMethod();
            if (readMethod != null) {
                try {
                    if (Collection.class.isAssignableFrom(descriptor.getPropertyType())) {
                        // Collections in model classes don't have setters
                        // Get the collection, clear it and refill it with source

                        final Collection<Object> sourceCollection = (Collection<Object>) readMethod.invoke(from);
                        final Collection<Object> targetCollection = (Collection<Object>) readMethod.invoke(entity);

                        targetCollection.clear();
                        targetCollection.addAll(sourceCollection);
                    } else {
                        final Object property = readMethod.invoke(from);
                        final Method writeMethod = descriptor.getWriteMethod();

                        if (writeMethod != null) {
                            writeMethod.invoke(entity, property);
                        }
                    }
                } catch (IllegalAccessException ignored) {
                    // If a getter or setter does not exist or is inaccessible just ignore the property
                }
            }
        }
    }

    public Object getId() {
        try {
            return PropertyUtils.getProperty(entity, "id");
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }

        return null;
    }
}
