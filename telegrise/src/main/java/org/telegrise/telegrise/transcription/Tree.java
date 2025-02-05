package org.telegrise.telegrise.transcription;


import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.TranscriptionManager;

/**
 * An interface of {@code <tree>} element.
 * <p>
 * An instance can be obtained from {@link SessionMemory} or {@link TranscriptionManager}.
 * This interface exposes only the necessary methods of tree implementation and is safe to use in production code.
 *
 * @since 0.11
 */
public interface Tree extends ElementBase {
    String getName();
    @Nullable String getDescription();
    @Nullable Class<?> getController();

    @Nullable String[] getCommands();
    @Nullable String[] getKeys();
    @Nullable String[] getCallbackTriggers();
    @Nullable Expression<Boolean> getPredicateExpression();

    @Nullable String[] getChatTypes();
    @Nullable String[] getScopes();
    @Nullable Integer getAccessLevel();
}
