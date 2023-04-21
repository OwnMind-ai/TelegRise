package org.telegram.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Points to the method that will be called after the tree is closed
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnClose {
}
