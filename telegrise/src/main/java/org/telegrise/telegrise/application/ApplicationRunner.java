package org.telegrise.telegrise.application;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.elements.BotTranscription;

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

//    ApplicationRunner WEBHOOK = (consumer, token, bot) -> {
//        Logger log = LoggerFactory.getLogger(ApplicationRunner.class);
//        Webhook webhook = bot.getHead().getWebhook();
//        ResourcePool pool = new ResourcePool();
//        boolean enableLogging = webhook.getEnableRequestLogging().generate(pool);
//        TelegramClient client = bot.produceClient();
//
//        try {
//            client.execute(SetWebhook.builder()
//                    .url(webhook.getUrl().generate(pool))
//                    .certificate(webhook.getCertificate() == null ? null : new InputFile(webhook.getCertificate().generate(pool)))
//                    .dropPendingUpdates(GeneratedValue.generate(webhook.getDropPendingUpdates(), pool))
//                    .ipAddress(GeneratedValue.generate(webhook.getIpAddress(), pool))
//                    .secretToken(GeneratedValue.generate(webhook.getSecretToken(), pool))
//                    .maxConnections(GeneratedValue.generate(webhook.getMaxConnections(), pool))
//                    .allowedUpdates(webhook.getAllowedUpdates() == null ? List.of() : List.of(webhook.getAllowedUpdates()))
//                    .build());
//
//            Javalin.create(javalinConfig -> {
//                javalinConfig.http.defaultContentType = ContentType.JSON;
//                javalinConfig.requestLogger.http((ctx, time) -> {
//                    if (enableLogging)
//                        log.info("'{}' request received from address '{}'", ctx.method(), ctx.req().getRemoteAddr());
//                });
//
//                javalinConfig.registerPlugin(new SslPlugin(config -> {
//                    if (webhook.getUseHttps().generate(pool)){
//                        if (webhook.getKeyStorePath() != null && webhook.getKeyStorePassword() != null)
//                            config.keystoreFromPath(webhook.getKeyStorePath().generate(pool), webhook.getKeyStorePassword().generate(pool));
//
//                        config.secure = true;
//                        config.securePort = webhook.getPort().generate(pool);
//                        javalinConfig.bundledPlugins.enableSslRedirects();
//                    } else {
//                        config.secure = false;
//                        config.insecurePort = webhook.getPort().generate(pool);
//                    }
//                    config.insecure = !config.secure;
//                }));
//            })
//            .events(events -> {
//                events.serverStarted(() -> log.info("Webhook bot server have been successfully started"));
//                events.serverStartFailed(() -> log.info("Webhook bot server have failed to start"));
//                events.serverStopped(() -> log.info("Webhook bot server have been stopped"));
//            })
//            .post("/", ctx -> {
//                Update update = ctx.bodyStreamAsClass(Update.class);
//                consumer.accept(update);
//                /*TODO HUGE (MAYBE) PERFORMANCE IMPROVEMENT POSSIBILITY
//                    We can leverage webhook's ability
//                    to return bot method as an answer to the request.
//                    Currently, webhooks bots behave as longpolling bots in this regard, making it less efficient.
//                    It is possible to capture the first BotApiMethod sent to BotSender to execute and pass it here,
//                    and execute other normally.
//                    However, this is really tricky and bug-prone to do.
//                    I am not sure if this even worth it.
//                    Maybe it will be beneficial to move session routing system here, idk.
//                */
//                ctx.status(200);
//            })
//            .start();
//            Thread.currentThread().join();
//        } catch (Exception e) {
//            log.error("Unable to register webhook bot (default configuration)", e);
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                client.execute(new DeleteWebhook());
//            } catch (TelegramApiException e) {
//                log.error("An error occurred while deleting webhook", e);
//            }
//        }
//    };
}
