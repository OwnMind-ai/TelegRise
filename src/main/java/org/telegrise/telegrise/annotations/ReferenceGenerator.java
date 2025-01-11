package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ReferenceGenerator {
    Class<?>[] parameters() default {};
}
