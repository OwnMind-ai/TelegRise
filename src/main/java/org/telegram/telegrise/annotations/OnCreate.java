package org.telegram.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Points to the method that will be called after the tree controller is created and the resources are injected
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnCreate {
}
