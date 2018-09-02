package nl.juraji.imagemanager.util.ui.modelfields;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.CheckBoxControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.FileTextFieldControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.TextFieldControlHandler;
import nl.juraji.imagemanager.util.ui.modelfields.handlers.URITextFieldControlHandler;

import javax.persistence.Entity;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        this.scanAndAddFields(entityClass, entity);
    }

    private void scanAndAddFields(Class<?> entityClass, Object entity) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Editable.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(Editable.class).order()))
                .forEachOrdered(field -> {
                    final Editable editable = field.getAnnotation(Editable.class);

                    final ControlHandler handler = getTypeBasedHandler(field.getType(), entity, field.getName(), editable.textArea());

                    handler.setNullable(editable.nullable());
                    handler.setDisabled(editable.disabled());

                    fields.add(new FieldDefinition(handler, editable.labelResource()));
                });

        // Recurse into superclass if available
        if (entityClass.getSuperclass() != null) {
            this.scanAndAddFields(entityClass.getSuperclass(), entity);
        }
    }

    public static EditableFieldContainer create(Object entity) {
        return new EditableFieldContainer(entity);
    }

    public Set<FieldDefinition> getFields() {
        return fields;
    }

    public void renderFieldsToGrid(GridPane gridPane, ResourceBundle resources) {
        renderFieldsToGrid(gridPane, resources, 0);
    }

    public void renderFieldsToGrid(GridPane gridPane, ResourceBundle resources, int startIndex) {
        final AtomicInteger rowIndexCounter = new AtomicInteger(startIndex);

        getFields().forEach(fieldDefinition -> {
            final Label label = new Label(resources.getString(fieldDefinition.getI18nLabelKey()));
            final Control control = fieldDefinition.getHandler().getControl();

            label.setPrefHeight(30.0);
            control.setPrefHeight(30.0);

            final int rowIndex = rowIndexCounter.getAndIncrement();
            gridPane.add(label, 0, rowIndex);
            gridPane.add(control, 1, rowIndex);
        });
    }

    private static ControlHandler getTypeBasedHandler(Class<?> propertyType, Object bean, String property, boolean isTextArea) {
        if (Boolean.class.isAssignableFrom(propertyType) || boolean.class.isAssignableFrom(propertyType)) {
            return new CheckBoxControlHandler(bean, property);
        } else if (File.class.isAssignableFrom(propertyType)) {
            return new FileTextFieldControlHandler(bean, property);
        } else if (URI.class.isAssignableFrom(propertyType)) {
            return new URITextFieldControlHandler(bean, property);
        } else {
            return new TextFieldControlHandler(bean, property, isTextArea);
        }
    }
}
