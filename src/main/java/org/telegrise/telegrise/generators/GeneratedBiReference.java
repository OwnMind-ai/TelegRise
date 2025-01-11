package org.telegrise.telegrise.generators;

@FunctionalInterface
public interface GeneratedBiReference<F, S, R> extends GeneratedReferenceBase {
    R run(F first, S second);

    @SuppressWarnings("unchecked")
    default R invokeUnsafe(Object f, Object s){
        return run((F) f, (S) s);
    }
}
