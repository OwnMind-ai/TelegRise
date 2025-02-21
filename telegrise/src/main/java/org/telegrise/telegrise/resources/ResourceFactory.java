package org.telegrise.telegrise.resources;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a factory that produces resource of type {@code T}
 * which can be injected using {@link org.telegrise.telegrise.annotations.Resource Resource} annotation.
 * <p>
 * Resource will be available for injection only by type {@code T}
 * (specifically {@code Class<T>} return by {@link #getResourceClass()}) and not by its superclasses or subclasses.
 *
 * @param <T> type of the produced resource
 * @since 0.2
 */
public interface ResourceFactory<T> {
    /**
     * Creates a resource factory that always returns a specified instance.
     *
     * @param instance instance of a resource to be injected
     * @param uClass class object as which resource would be injected
     * @return resource factory
     * @param <U> type of the resource
     */
    static <U> ResourceFactory<U> ofInstance(U instance, Class<U> uClass){
        return new ResourceFactory<>() {
            @Override
            public @NotNull Class<U> getResourceClass() {
                return uClass;
            }

            @Override
            public U getResource(Class<?> target) {
                return instance;
            }
        };
    }

    /**
     * Returns class object that will be used to determine applicable injection fields.
     *
     * @return type of the produced resource
     */
    @NotNull Class<T> getResourceClass();

    /**
     * Produces resource of the type {@code T} that will be injected to a class field
     * marked by {@link org.telegrise.telegrise.annotations.Reference Reference} annotation.
     * This method is called every time an injection is required.
     * Ideally, this method should be <i>pure</i>, but that is up to implementation and its use case.
     *
     * @param injectionPoint A class of the object that this resource being injected to (controller, handler, service, etc.)
     * @return An instance to be injected
     */
    T getResource(Class<?> injectionPoint);
}
