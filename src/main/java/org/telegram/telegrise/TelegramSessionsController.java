package org.telegram.telegrise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TelegramSessionsController {
    private final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    private final ConcurrentMap<UserIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
}
