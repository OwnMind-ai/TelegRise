package org.telegram.telegrise.core.parser;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ElementField {
    String name() default "";
    boolean isTextContext() default false;
    boolean nullable() default true;
    boolean expression() default false;
    double priority() default Double.NEGATIVE_INFINITY;
}
