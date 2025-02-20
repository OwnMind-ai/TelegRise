package org.telegrise.telegrise.application;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.head.Webhook;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class WebhookApplicationRunner implements ApplicationRunner{
    // TODO Consider using Javalin 7 when it becomes available, and they fix their vulnerabilities
    @Override
    public void run(Consumer<Update> consumer, String token, BotTranscription bot, ExecutorService executor) {
        Logger log = LoggerFactory.getLogger(ApplicationRunner.class);
        Webhook webhook = bot.getHead().getWebhook();
        ResourcePool pool = new ResourcePool();
        TelegramClient client = bot.produceClient();

        Server server = new Server();
        var sslContext = createSslContext(webhook, pool);
        try(ServerConnector connector = new ServerConnector(server, sslContext)) {
            client.execute(webhook.produceSetWebhook(pool));
            log.info("Webhook to url '{}' have been successively set", webhook.getUrl().generate(pool));

            var context = new ServletContextHandler();
            var holder = new ServletHolder(new WebhookServlet(
                    consumer, executor,
                    GeneratedValue.generate(webhook.getSecretToken(), pool)
            ));
            context.addServlet(holder, "/");

            connector.setPort(webhook.getPort().generate(pool));
            connector.setReusePort(false);
            server.setHandler(context);
            server.addConnector(connector);
            server.start();

            log.info("Webhook bot server have been successfully started");
            server.join();
        } catch (Exception e) {
            log.error("Unable to register webhook bot (default configuration)", e);
            throw new RuntimeException(e);
        } finally {
            try {
                client.execute(new DeleteWebhook());
            } catch (TelegramApiException e) {
                log.error("An error occurred while deleting webhook", e);
            }
        }
    }

    private static @Nullable SslContextFactory.Server createSslContext(Webhook webhook, ResourcePool pool) {
        if (!webhook.getUseHttps().generate(pool)) return null;
        if (webhook.getKeyStorePath() == null)
            throw new TranscriptionParsingException("Unable to create SSL context: https is used but no keyStorePath specified", webhook.getElementNode());

        var context = new SslContextFactory.Server();
        context.setKeyStorePath(webhook.getKeyStorePath().generate(pool));
        context.setKeyStorePassword(GeneratedValue.generate(webhook.getKeyStorePassword(), pool));

        return context;
    }
}
