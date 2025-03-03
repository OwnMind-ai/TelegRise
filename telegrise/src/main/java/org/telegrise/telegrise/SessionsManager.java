package org.telegrise.telegrise;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * An object that manages sessions in the application.
 * <p>
 * Session manager can be injected using
 * {@link org.telegrise.telegrise.annotations.Resource Resource} annotation.
 * It allows creating new sessions, loading serialized ones,
 * killing existing session and retrieving session memory based on session identifier.
 *
 * @see SessionIdentifier
 * @see SessionMemory
 * @since 0.9
 */
@SuppressWarnings("unused")
public interface SessionsManager {
    /**
     * Loads session based on information in provided session memory.
     * Typically, this method is used as a means to load deserialized memory for external services and servers.
     * @param memory memory of session to be loaded
     */
    void loadSession(SessionMemory memory);

    /**
     * Destroys session with a specified identifier.
     * Killed session can reappear (with a new state)
     * if update with the same credentials causes the creating of a new session.
     *
     * @param identifier credentials of the session
     */
    void killSession(SessionIdentifier identifier);

    /**
     * Creates to session based on specified identifier without the need of actual interaction with the user.
     * @param identifier credentials of the session
     */
    void createSession(SessionIdentifier identifier, @Nullable String languageCode);

    /**
     * Forces session to be recreated with a blank state.
     * <p>
     * Equivalent to:
     * <pre>{@code
     * killSession(identifier);
     * createSession(identifier);
     * }</pre>
     * @param sessionIdentifier credentials of the session
     */
    void reinitializeSession(SessionIdentifier sessionIdentifier);

    /**
     * Retrieves session memory of session with the same identifier as specified, or null if no such session exists.
     * @param sessionIdentifier credentials of the session
     * @return session memory of the session or null
     */
    @Nullable SessionMemory getSessionMemory(SessionIdentifier sessionIdentifier);

    /**
     * Returns a transcription manager for this bot.
     * Returned transcription manager is not attached to a session and all session-related methods will fail
     * @return transcription manager
     */
    TranscriptionManager getTranscriptionManager();

    /**
     * Returns a transcription manager for a specified user.
     * @return transcription manager
     */
    TranscriptionManager getTranscriptionManager(SessionIdentifier identifier);


    /**
     * Registers a functional callback that will be invoked when a user session is being destroyed.
     * @param callback function to be executed
     */
    void registerSessionDestructionCallback(BiConsumer<SessionIdentifier, SessionMemory> callback);
}
