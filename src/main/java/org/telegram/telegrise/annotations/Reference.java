package org.telegram.telegrise.annotations;

import org.telegram.telegrise.caching.CachingStrategy;

import java.lang.annotation.*;

/**
 * Indicates that an annotated method can be used as the element's expression using syntax '#method' or 'Class#method'.
 * <p>
 * The annotated method must be public and allowed to have no parameters,
 * one parameter of type <a href="#{@link}">{@link org.telegram.telegrambots.meta.api.objects.Update Update}</a>,
 * or multiple parameters in any order if specified for a specific  element.
 * If the method is meant to be used in a method chain, the annotated method should have only one parameter of type that will be passed to it according to the chain.
 * </p><p>
 * Depending on how method is declared, reference syntax changes:
 * <ul>
 *  <li>If method is non-static, reference syntax is '#methodName'</li>
 *  <li>If method is static, reference syntax is 'ClassName#methodName'</li>  
 * </ul>
 * </p>
 * 
 * @since 0.4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})  //TODO add ElementType.FIELD, it would be nice to have getters
public @interface Reference {
    /** Indicates the caching strategy for specific method reference. See <a href="#{@link}">{@link org.telegram.telegrise.caching.CachingStrategy CachingStrategy}</a> for detailed description.
     *
     * @return caching strategy for the method
     * @see CachingStrategy
     */
    CachingStrategy caching() default CachingStrategy.NONE;
}
