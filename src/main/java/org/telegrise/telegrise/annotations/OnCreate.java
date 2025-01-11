package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that an annotated method will be called after the tree has been created and resources have been injected.
 * 
 * @see TreeController
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnCreate {
}
