package org.telegrise.telegrise.annotations;

import org.telegrise.telegrise.caching.CachingStrategy;

import java.lang.annotation.*;

/**
 * Indicates that an annotated method can be used as the expression using syntax '#method' or 'Class#method'.
 * <p>
 * The annotated method must be public
 * and allowed to have any parameters in any order depending on the use case.
 * </p><p>
 * Depending on how the method is declared, reference syntax changes:
 * <ul>
 *  <li>If a method is non-static, reference syntax is '#methodName'</li>
 *  <li>If a method is static, reference syntax is 'ClassName#methodName'</li>
 * </ul>
 * </p>
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Reference {
    /** Indicates the caching strategy for specific method reference. See <a href="#{@link}">{@link CachingStrategy CachingStrategy}</a> for detailed description.
     *
     * @return caching strategy for the method
     * @see CachingStrategy
     */
    CachingStrategy caching() default CachingStrategy.NONE;
}
