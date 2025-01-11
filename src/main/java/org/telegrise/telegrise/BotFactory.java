package org.telegrise.telegrise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.TelegramWebhookBot;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;

import java.util.List;
import java.util.Optional;

public class BotFactory {
    public static LongPollingUpdateConsumer createLongPooling(TelegramSessionsController controller){
        return (LongPollingSingleThreadUpdateConsumer) controller::onUpdateReceived;
    }

    public static TelegramWebhookBot createWebhookBot(TelegramSessionsController controller, String token){
        var webhook = controller.getTranscription().getHead().getWebhook();
        String path = "/" + controller.getTranscription().getUsername().generate(new ResourcePool());

        return new TelegramWebhookBot() {
            private static final Logger log = LoggerFactory.getLogger(BotFactory.class);
            private final TelegramClient client = new OkHttpTelegramClient(token);

            @Override
            public void runDeleteWebhook() {
                try {
                    client.execute(DeleteWebhook.builder().build());
                } catch (TelegramApiException e) {
                    log.error("An error occurred while deleting webhook", e);
                }
            }

            @Override
            public void runSetWebhook() {
                var resource = new ResourcePool();
                try {
                    client.execute(SetWebhook.builder()
                            .url(webhook.getUrl().generate(resource))
                            .certificate(Optional.ofNullable(GeneratedValue.generate(webhook.getCertificate(), resource))
                                    .map(InputFile::new).orElse(null))
                            .ipAddress(GeneratedValue.generate(webhook.getIpAddress(), resource))
                            .maxConnections(GeneratedValue.generate(webhook.getMaxConnections(), resource))
                            .dropPendingUpdates(GeneratedValue.generate(webhook.getDropPendingUpdates(), resource))
                            .secretToken(GeneratedValue.generate(webhook.getSecretToken(), resource))
                            .allowedUpdates(webhook.getAllowedUpdates() == null ? List.of() : List.of(webhook.getAllowedUpdates()))
                            .build());
                } catch (TelegramApiException e) {
                    log.error("An error occurred while deleting webhook", e);
                }
            }

            @Override
            public BotApiMethod<?> consumeUpdate(Update update) {
                try {
                    controller.onUpdateReceived(update);
                } catch (Exception e){
                    log.error("An error occurred while processing update", e);
                }

                /*TODO HUGE (MAYBE) PERFORMANCE IMPROVEMENT POSSIBILITY
                    We can leverage webhook's ability to return bot method as an answer to the request.
                    Currently, webhooks bots behave as longpolling bots in this regard, making it less efficient.
                    It is possible to capture the first BotApiMethod sent to BotSender to execute and pass it here,
                    and execute other normally.
                    However, this is really tricky and bug-prone to do. I am not sure if this even worth it.
                    Maybe it will be beneficial to move session routing system here, idk.
                */
                return null;
            }

            @Override
            public String getBotPath() {
                return path;
            }
        };
    }
}
