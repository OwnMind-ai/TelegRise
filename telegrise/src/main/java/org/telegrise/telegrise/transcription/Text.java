package org.telegrise.telegrise.transcription;

import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.TranscriptionManager;

/**
 * An interface of {@code <text>} element.
 * <p>
 * An instance can be obtained from {@link TranscriptionManager#get(String, Class)}.
 * This interface exposes only the necessary methods of text implementation and is safe to use in production code.
 *
 * @since 0.11
 */
public interface Text extends ElementBase {
    // Although the actual value can be null, for the instance to be reachable outside the module, it must have a name
    String getName();

    Expression<String> getTextExpression();
}
