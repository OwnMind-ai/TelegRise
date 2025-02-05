package org.telegrise.telegrise.transcription;

import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.TranscriptionManager;

/**
 * An interface of {@code <branch>} element.
 * <p>
 * An instance can be obtained from {@link SessionMemory} or {@link TranscriptionManager}.
 * This interface exposes only the necessary methods of branch implementation and is safe to use in production code.
 *
 * @since 0.11
 */
public interface Branch extends ElementBase{
    @Nullable String getName();

    @Nullable String[] getKeys();
    @Nullable String[] getCallbackTriggers();
    @Nullable Expression<Boolean> getWhenExpression();

    @Nullable Expression<Void> getToInvokeExpression();
}
