package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Indicates that an annotated method will be called after the tree is closed.
 * 
 * @see TreeController
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnClose {
}
