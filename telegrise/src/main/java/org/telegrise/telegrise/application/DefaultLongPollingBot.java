package org.telegrise.telegrise.application;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class represents a long-polling bot application implementation, that uses provided executor service
 * ({@link Executors#newVirtualThreadPerTaskExecutor()} by default).
 * Instance of this class is meant to be used as a wrapper for {@link org.telegrise.telegrise.TelegramSessionsController TelegramSessionController}
 * that provides a concurrent execution (based on {@code executor}) and fits into corresponding
 * {@link org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication#registerBot(String, Supplier, Function, LongPollingUpdateConsumer) registerBot} method.
 *
 * @see DefaultUpdateConsumer
 * @see ApplicationRunner
 * @since 0.10
 */
@Setter
final class DefaultLongPollingBot extends DefaultUpdateConsumer implements LongPollingUpdateConsumer {
    DefaultLongPollingBot(Consumer<Update> updateConsumer, @Nullable ExecutorService executor) {
        super(updateConsumer, executor);
    }

    @Override
    public void consume(List<Update> list) {
        for(Update update : list)
            executor.submit(() -> updateConsumer.accept(update));
    }
}
