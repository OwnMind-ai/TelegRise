package org.telegrise.telegrise;

public interface Service extends Runnable{
    default Integer threadPriority() {
        return null;
    }
    default void onInterruption(){};
}
