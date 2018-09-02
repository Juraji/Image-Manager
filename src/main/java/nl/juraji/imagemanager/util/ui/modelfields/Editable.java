package nl.juraji.imagemanager.util.ui.modelfields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Editable {
    String labelResource();
    int order();
    boolean nullable() default false;
    boolean disabled() default false;
    boolean textArea() default false;
}
