package org.telegrise.telegrise;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface SessionsManager {
    void loadSession(SessionMemory memory);
    void killSession(SessionIdentifier identifier);
    void createSession(SessionIdentifier identifier);
    void reinitializeSession(SessionIdentifier sessionIdentifier);
    @Nullable SessionMemory getSessionMemory(SessionIdentifier sessionIdentifier);
}
