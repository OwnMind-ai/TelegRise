package org.telegram.telegrise;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.elements.BotTranscription;

import java.util.function.Consumer;

public class BotFactory {
    public static TelegramLongPollingBot createLongPooling(BotTranscription transcription, Consumer<Update> updateConsumer){
        return new TelegramLongPollingBot(transcription.getToken()) {
            @Override
            public void onUpdateReceived(Update update) {
                updateConsumer.accept(update);
            }

            @Override
            public String getBotUsername() {
                return transcription.getUsername();
            }
        };
    }
}
