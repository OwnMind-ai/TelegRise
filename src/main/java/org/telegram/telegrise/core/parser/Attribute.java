package org.telegram.telegrise.core.parser;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Attribute {
    String name() default "";
    boolean isTextContext() default false;
    boolean nullable() default true;

    double priority() default 0;
}
