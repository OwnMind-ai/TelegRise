package org.telegrise.telegrise;

/**
 * Represents a service that will be run in a separate thread right after the start of the application.
 * Services support injection of necessary resources using {@link org.telegrise.telegrise.annotations.Resource Resource} annotation.
 *
 * @since 0.3
 */
public interface Service extends Runnable{
    default Integer threadPriority() {
        return null;
    }
    default void onInterruption(){}
}
