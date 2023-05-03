package org.telegram.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a primary handler.
 * The annotated must implement the interface <a href="#{@link}">{@link org.telegram.telegrise.PrimaryHandler PrimaryHandler}</a>.
 * To add primary handler to the <code>TelegRiseApplication</code> use <code>addHandler</code> method.
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    boolean absolute() default false;
}
