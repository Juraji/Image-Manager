package nl.juraji.imagemanager.ui;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Created by Juraji on 10-9-2018.
 * Image Manager
 */
public class ModelUtils {

    /**
     * Copy model bean properties
     *
     * @param destination The destination bean
     * @param source      The source bean
     * @param <T>         Generic type to force argument type equality
     * @throws IllegalAccessException    By {@link PropertyUtils#getProperty(Object, String)}
     * @throws InvocationTargetException By {@link PropertyUtils#getProperty(Object, String)}
     */
    @SuppressWarnings("unchecked")
    public static <T> void copyProperties(T destination, T source) throws IllegalAccessException, InvocationTargetException {
        final Field[] declaredFields = source.getClass().getDeclaredFields();

        for (Field field : declaredFields) {
            try {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    // Collections in model classes don't have setters
                    // Get the collection, clear it and refill it with source

                    final Collection<Object> sourceCollection = (Collection<Object>) PropertyUtils.getProperty(source, field.getName());
                    final Collection<Object> targetCollection = (Collection<Object>) PropertyUtils.getProperty(destination, field.getName());

                    targetCollection.clear();
                    targetCollection.addAll(sourceCollection);

                } else {
                    final Object property = PropertyUtils.getProperty(source, field.getName());
                    PropertyUtils.setProperty(destination, field.getName(), property);
                }
            } catch (NoSuchMethodException ignored) {
                // If a getter or setter does not exist just ignore the property
            }
        }
    }
}
