package org.telegrise.telegrise.application;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * A base class for all default bot application implementations, such as {@link DefaultLongPollingBot}.
 * Instance of this class is meant to be used as a wrapper for {@link org.telegrise.telegrise.TelegramSessionsController TelegramSessionController}
 * that provides a concurrent execution (based on {@code executor}).
 *
 * @see DefaultLongPollingBot
 * @since 0.10
 */
abstract sealed class DefaultUpdateConsumer permits DefaultLongPollingBot {
    protected final Consumer<Update> updateConsumer;
    protected final ExecutorService executor;

    protected DefaultUpdateConsumer(Consumer<Update> updateConsumer, @Nullable ExecutorService executor) {
        this.updateConsumer = updateConsumer;
        this.executor = executor == null ? Executors.newVirtualThreadPerTaskExecutor() : executor;
    }
}
