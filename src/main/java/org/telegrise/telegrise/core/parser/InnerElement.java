package org.telegrise.telegrise.core.parser;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InnerElement {
    boolean nullable() default true;
    double priority() default 0;
}
