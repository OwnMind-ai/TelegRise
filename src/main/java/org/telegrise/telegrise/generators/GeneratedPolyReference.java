package org.telegrise.telegrise.generators;

/**
 * Functional instance of this interface represents a generated <b>reference</b>
 * that takes multiple parameters, that passed as array of <code>Object</code>s
 * and returns a value of type <code>R</code>.
 *
 * @param <R> output type
 * @see org.telegrise.telegrise.annotations.ReferenceGenerator
 * @since 0.8
 */
@FunctionalInterface
public non-sealed interface GeneratedPolyReference<R> extends GeneratedReferenceBase {
    R run(Object[] parameters);
}
