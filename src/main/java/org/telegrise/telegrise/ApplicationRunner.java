package org.telegrise.telegrise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

/**
 * Represents a function that will run the application.
 * <p>
 * Since webhook support is temporarily suspended due to its complexity, this object allows for a third-party server implementation,
 * which can include webhook support. Use of this object is deemed risky as it <b>will</b> be removed in the future.
 *
 * @since 0.10
 */
@FunctionalInterface
public interface ApplicationRunner {
    void run(Consumer<Update> consumer, String token);

    ApplicationRunner LONG_POLLING = (consumer, token) -> {
        Logger log = LoggerFactory.getLogger(ApplicationRunner.class);

        try (var api = new TelegramBotsLongPollingApplication()) {
            api.registerBot(token, (LongPollingSingleThreadUpdateConsumer) consumer::accept);
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Unable to register long-polling bot (default configuration)", e);
            throw new RuntimeException(e);
        }
    };
}
