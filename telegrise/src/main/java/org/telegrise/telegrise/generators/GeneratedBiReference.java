package org.telegrise.telegrise.generators;

/**
 * Functional instance of this interface represents a generated <b>reference</b>
 * that takes two parameters of types <code>F</code> and <code>S</code>
 * and returns a value of type <code>R</code>.
 *
 * @param <F> first input parameter type
 * @param <S> second input parameter type
 * @param <R> output type
 * @see org.telegrise.telegrise.annotations.ReferenceGenerator
 * @since 0.8
 */
@FunctionalInterface
public non-sealed interface GeneratedBiReference<F, S, R> extends GeneratedReferenceBase {
    R run(F first, S second);

    @SuppressWarnings("unchecked")
    default R invokeUnsafe(Object f, Object s){
        return run((F) f, (S) s);
    }
}
