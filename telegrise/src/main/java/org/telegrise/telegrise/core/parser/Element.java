package org.telegrise.telegrise.core.parser;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Element {
    String name();
    boolean checkInner() default true;
    boolean finishAfterParsing() default false;
}
