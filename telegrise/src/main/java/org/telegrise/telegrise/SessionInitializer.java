package org.telegrise.telegrise;

import java.util.List;

/**
 * Represents an object that handles initialization of new sessions.
 * Implementation can use {@link org.telegrise.telegrise.annotations.Resource Resource} annotation to
 * inject the necessary resources.
 * <p>
 * The {@link #initialize(SessionMemory)} method will be called upon creation of new session before any handling of the update.
 * The {@link #getInitializionList()} specifies the list of sessions
 * that must be initialized at the start of the application.
 *
 * @since 0.5
 */
public interface SessionInitializer{
    /**
     * Configurations and prepares newly created session.
     * @param memory memory of the new session
     */
    void initialize(SessionMemory memory);

    /**
     * @return list of sessions that must be initialized at the start of the application
     */
    default List<SessionIdentifier> getInitializionList(){ return List.of(); }
}
