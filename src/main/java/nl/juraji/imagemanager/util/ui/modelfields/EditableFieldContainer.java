package nl.juraji.imagemanager.util.ui.modelfields;

import nl.juraji.imagemanager.util.ui.modelfields.handlers.CheckBoxControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.FileTextFieldControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.TextFieldControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.URITextFieldControlHandler;

import javax.persistence.Entity;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
public final class EditableFieldContainer {
    private final Set<FieldDefinition> fields;

    private EditableFieldContainer(Object entity) {
        this.fields = new LinkedHashSet<>();

        final Class<?> entityClass = entity.getClass();
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not a @javax.persistence.Entity");
        }

        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Editable.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(Editable.class).order()))
                .forEachOrdered(field -> {
                    final Editable editable = field.getAnnotation(Editable.class);

                    final ControlHandler handler = getTypeBasedHandler(field.getType(), entity, field.getName(), editable.nullable());
                    fields.add(new FieldDefinition(handler, editable.labelResource()));
                });
    }

    public static EditableFieldContainer create(Object entity) {
        return new EditableFieldContainer(entity);
    }

    public Set<FieldDefinition> getFields() {
        return fields;
    }

    private static ControlHandler getTypeBasedHandler(Class<?> propertyType, Object bean, String property, boolean nullable) {
        if (Boolean.class.isAssignableFrom(propertyType) || boolean.class.isAssignableFrom(propertyType)) {
            return new CheckBoxControlHandler(bean, property, false);
        } else if (File.class.isAssignableFrom(propertyType)) {
            return new FileTextFieldControlHandler(bean, property, nullable);
        } else if (URI.class.isAssignableFrom(propertyType)) {
            return new URITextFieldControlHandler(bean, property, nullable);
        } else {
            return new TextFieldControlHandler(bean, property, nullable);
        }
    }

}
