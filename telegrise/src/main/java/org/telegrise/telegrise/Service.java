package org.telegrise.telegrise;

public interface Service extends Runnable{
    default Integer threadPriority() {
        return null;
    }

    default Boolean isDaemon(){
        return null;
    }
}
