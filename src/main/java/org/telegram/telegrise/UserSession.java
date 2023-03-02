package org.telegram.telegrise;

public class UserSession implements Runnable{
    private final SessionMemory sessionMemory;

    public UserSession(SessionMemory sessionMemory) {
        this.sessionMemory = sessionMemory;
    }

    @Override
    public void run() {

    }
}
