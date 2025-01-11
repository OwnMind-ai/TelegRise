package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.PrimaryHandler;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a primary handler.
 * The annotated must implement the interface <a href="#{@link}">{@link PrimaryHandler PrimaryHandler}</a>.
 * To add primary handler to the <code>TelegRiseApplication</code> use <code>addHandler</code> method.
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    boolean absolute() default false;
    boolean independent() default false;
    boolean afterTrees() default false;
    int priority() default 0;
}
