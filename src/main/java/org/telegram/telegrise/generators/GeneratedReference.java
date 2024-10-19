package org.telegram.telegrise.generators;

@FunctionalInterface
public interface GeneratedReference<T, R> extends GeneratedReferenceBase {
    R run(T t);

    @SuppressWarnings("unchecked")
    default R invokeUnsafe(Object i){
        return run((T) i);
    }
}
