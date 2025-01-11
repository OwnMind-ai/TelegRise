package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.resources.ResourceFactory;

import java.lang.annotation.*;

/**
 * Indicates that resource with a corresponding type will be injected to an annotated field.
 * Custom resources can be added by implementing <a href="#{@link}">{@link ResourceFactory ResourceFactory}</a> interface.
 * 
 * @since 0.4
 * @see ResourceFactory
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Resource {
}
