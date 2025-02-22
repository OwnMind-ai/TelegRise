package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.resources.ResourceFactory;

import java.lang.annotation.*;

/**
 * Indicates that a resource with a corresponding type will be injected to an annotated field.
 * Custom resources can be added by implementing {@link ResourceFactory ResourceFactory} interface.
 * 
 * @since 0.4
 * @see ResourceFactory
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Resource {
}
