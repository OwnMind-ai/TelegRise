package org.telegrise.telegrise.application;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Represents a function that will run the application.
 *
 * @since 0.10
 */
@FunctionalInterface
public interface ApplicationRunner {
    void run(Consumer<Update> consumer, String token, BotTranscription transcription, @Nullable ExecutorService executor);

    ApplicationRunner LONG_POLLING = (consumer, token, bot, executorService) -> {
        Logger log = LoggerFactory.getLogger(ApplicationRunner.class);

        try (var api = new TelegramBotsLongPollingApplication()) {
            api.registerBot(
                    token,
                    bot::getTelegramUrl,
                    new DefaultGetUpdatesGenerator(),
                    new DefaultLongPollingBot(consumer, executorService)
            );
            log.info("Long-polling bot server have been successfully started");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Unable to register long-polling bot (default configuration)", e);
            throw new RuntimeException(e);
        }
    };

    static ApplicationRunner getWebhookRunner() {
        try {
            Class<?> runnerClass = Class.forName("org.telegrise.telegrise.application.WebhookApplicationRunner");
            return (ApplicationRunner) runnerClass.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new TelegRiseRuntimeException("Unable to start webhook server: no webhook application runner detected." +
                    "Please, add a dependency for webhook server of artifact 'telegrise-webhooks'.");
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
