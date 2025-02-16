package org.telegrise.telegrise.core.parser;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Attribute {
    String name();
    boolean isTextContext() default false;
    boolean nullable() default true;

    double priority() default 0;
//    String types(); TODO, for docs
}
