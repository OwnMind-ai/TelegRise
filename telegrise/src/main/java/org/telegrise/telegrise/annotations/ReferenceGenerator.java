package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.generators.GeneratedReferenceBase;

import java.lang.annotation.*;

/**
 * Indicates that an annotated method can be used as the expression using syntax '::method(...)'
 * or 'Class::method(...)'.
 * This method must return an instance of {@link GeneratedReferenceBase GeneratedReferenceBase}.
 * <p>
 * <b>Generators</b> are high-order function that produce virtual {@link Reference references}.
 * For instance, in expression <code>#getText -> ::matches("\\d+")</code> generator <code>matches</code>
 * internally produces reference that would look like this Java code: <code>string -> string.matches("\\d+")</code>.
 *
 * @since 0.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ReferenceGenerator {
    Class<?>[] parameters() default {};
}
