package org.telegrise.telegrise.annotations;

import java.lang.annotation.*;

/**
 * Values for the parameters of {@link Reference reference} and {@link ReferenceGenerator generator} methods
 * that annotated by this annotation would be passed regardless of an actual input
 * coming from piping operation {@code ->}.
 *
 * <pre>
 * {@code
 * @Reference
 * public void accept(Integer passedIn, @HiddenParameter SessionMemory memory){ }
 *
 * <invoke method="#getInt -> #accept"/>
 * }
 * </pre>
 *
 * @since 0.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface HiddenParameter {
}
