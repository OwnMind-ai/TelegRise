package org.telegram.telegrise.generators;

@FunctionalInterface
public interface GeneratedVoidReference<T> extends GeneratedReferenceBase {
    void run(T t);

    @SuppressWarnings("unchecked")
    default void invokeUnsafe(Object i){
        run((T) i);
    }
}