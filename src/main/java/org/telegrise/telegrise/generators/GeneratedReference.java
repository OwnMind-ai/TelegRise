package org.telegrise.telegrise.generators;

/**
 * Functional instance of this interface represents a generated <b>reference</b>
 * that takes one parameter of type <code>T</code>
 * and returns a value of type <code>R</code>.
 *
 * @param <T> single input parameter type
 * @param <R> output type
 * @see org.telegrise.telegrise.annotations.ReferenceGenerator
 * @since 0.8
 */
@FunctionalInterface
public non-sealed interface GeneratedReference<T, R> extends GeneratedReferenceBase {
    R run(T t);

    @SuppressWarnings("unchecked")
    default R invokeUnsafe(Object i){
        return run((T) i);
    }
}
