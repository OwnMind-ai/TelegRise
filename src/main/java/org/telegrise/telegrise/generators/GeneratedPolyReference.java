package org.telegrise.telegrise.generators;

@FunctionalInterface
public interface GeneratedPolyReference<R> extends GeneratedReferenceBase {
    R run(Object[] parameters);
}
