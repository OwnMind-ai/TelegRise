package org.telegram.telegrise.senders.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TelegramApiRuntimeException;
import org.telegram.telegrise.senders.BotSender;

import java.io.Serializable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class EditableMessageActionBuilder extends MessageActionBuilder{
    private static final Logger LOGGER = LoggerFactory.getLogger(EditableMessageActionBuilder.class);
    public EditableMessageActionBuilder(BotSender sender, Message message) {
        super(sender, message);
    }

    public Serializable edit(String text){
        return edit(text, null);
    }

    public Serializable edit(InlineKeyboardMarkup markup){
        return edit(null, markup);
    }

    public Serializable edit(String text, InlineKeyboardMarkup markup){
        assert text != null || markup != null;
        if (message.getMediaGroupId() != null && markup != null)
            throw new TelegRiseRuntimeException("Unable to edit inline keyboard to the media-group message");

        try {
            if (text == null){
                return this.sender.execute(EditMessageReplyMarkup.builder()
                        .messageId(message.getMessageId())
                        .chatId(chatId)
                        .replyMarkup(markup)
                        .build());
            } else {
                if (message.hasPhoto() || message.hasAudio() || message.hasVideo() || message.hasDocument()) {
                    return this.sender.execute(EditMessageCaption.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .parseMode(entities != null ? null : parseMode)
                            .caption(text)
                            .replyMarkup(markup)
                            .captionEntities(entities)
                            .build());
                } else {
                    return this.sender.execute(EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .parseMode(entities != null ? null : parseMode)
                            .text(text)
                            .disableWebPagePreview(disableWebPagePreview)
                            .replyMarkup(markup)
                            .linkPreviewOptions(linkPreviewOptions)
                            .entities(entities)
                            .build());
                }
            }
        } catch (TelegramApiException e) {
            LOGGER.error("An error occurred while editing the message", e);
            throw new TelegramApiRuntimeException(e);
        }
    }

    public boolean delete(){
        try {
            return this.sender.execute(DeleteMessage.builder()
                            .chatId(chatId).messageId(message.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            LOGGER.error("An error occurred while deleting the message", e);
            throw new RuntimeException(e);
        }
    }
}
