package org.telegrise.telegrise.transcription;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.TranscriptionManager;

/**
 * An interface of {@code <keyboard>} element.
 * <p>
 * An instance can be obtained from {@link TranscriptionManager#get(String, Class)}.
 * This interface exposes only the necessary methods of keyboard implementation and is safe to use in production code.
 *
 * @since 0.11
 */
public interface Keyboard extends ElementBase {
    // Although the actual value can be null, for the instance to be reachable outside the module, it must have a name
    String getName();
    @Nullable String getType();
    Expression<ReplyKeyboard> getMarkupExpression();
}
