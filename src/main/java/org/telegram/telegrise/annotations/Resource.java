package org.telegram.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that resource with a corresponding type will be injected to an annotated field.
 * Custom resources can be added by implementing <a href="#{@link}">{@link org.telegram.telegrise.resources.ResourceFactory ResourceFactory}</a> interface.
 * 
 * @since 0.4
 * @see org.telegram.telegrise.resources.ResourceFactory
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Resource {
}
