package org.telegrise.telegrise.generators;

/**
 * A functional instance of this interface represents a generated <b>reference</b> that takes one parameter
 * but returns nothing.
 *
 * @param <T> single input parameter type
 * @see org.telegrise.telegrise.annotations.ReferenceGenerator
 * @since 0.8
 */
@FunctionalInterface
public non-sealed interface GeneratedVoidReference<T> extends GeneratedReferenceBase {
    void run(T t);

    @SuppressWarnings("unchecked")
    default void invokeUnsafe(Object i){
        run((T) i);
    }
}