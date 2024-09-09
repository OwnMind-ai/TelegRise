package org.telegram.telegrise.senders.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TelegramApiRuntimeException;
import org.telegram.telegrise.senders.BotSender;
import java.util.List;

import java.io.Serializable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class EditableMessageActionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EditableMessageActionBuilder.class);
    protected final Message message;
    protected final BotSender sender;

    protected final String chatId;
    protected final Integer messageThreadId;
    protected String text;
    protected String parseMode = BotSender.DEFAULT_PARSE_MODE;
    protected Boolean disableWebPagePreview;
    protected Boolean disableNotification;
    protected Integer replyToMessageId;
    protected ReplyKeyboard replyMarkup;
    protected List<MessageEntity> entities;
    protected Boolean allowSendingWithoutReply;
    protected Boolean protectContent;
    protected LinkPreviewOptions linkPreviewOptions;
    protected ReplyParameters replyParameters;
    protected String businessConnectionId;

    private SessionMemoryImpl memory;
    private boolean sneaky;

    public EditableMessageActionBuilder(BotSender sender, Message message, SessionMemoryImpl memory) {
        this.message = message;
        this.sender = sender;
        this.memory = memory;
        sneaky = memory == null;

        this.chatId = String.valueOf(message.getChatId());
        this.messageThreadId = message.getMessageThreadId();
    }

    public EditableMessageActionBuilder sneaky(){
        if (memory == null) 
            logger.warn("Sender is already sneaky since there is no session memory");
            
        sneaky = true;
        return this;
    }

    public EditableMessageActionBuilder parseMode(String parseMode){
        this.parseMode = parseMode;
        return this;
    }

    public EditableMessageActionBuilder replyParameters(ReplyParameters replyParameters){
        this.replyParameters = replyParameters;
        return this;
    }

    public EditableMessageActionBuilder disableNotification(Boolean disableNotification){
        this.disableNotification = disableNotification;
        return this;
    }

    public EditableMessageActionBuilder entities(List<MessageEntity> entities){
        this.entities = entities;
        return this;
    }

    public EditableMessageActionBuilder replyToMessageId(Integer replyToMessageId){
        this.replyToMessageId = replyToMessageId;
        return this;
    }

    public EditableMessageActionBuilder replyMarkup(ReplyKeyboard replyMarkup){
        this.replyMarkup = replyMarkup;
        return this;
    }

    public EditableMessageActionBuilder allowSendingWithoutReply(Boolean allowSendingWithoutReply){
        this.allowSendingWithoutReply = allowSendingWithoutReply;
        return this;
    }

    public EditableMessageActionBuilder businessConnectionId(String businessConnectionId){
        this.businessConnectionId = businessConnectionId;
        return this;
    }

    public EditableMessageActionBuilder protectContent(Boolean protectContent){
        this.protectContent = protectContent;
        return this;
    }

    public EditableMessageActionBuilder disableWebPagePreview(Boolean disableWebPagePreview){
        this.disableWebPagePreview = disableWebPagePreview;
        return this;
    }

    public EditableMessageActionBuilder linkPreviewOptions(LinkPreviewOptions linkPreviewOptions){
        this.linkPreviewOptions = linkPreviewOptions;
        return this;
    }

    public Message send(String text){
        this.text = text;
        return this.executeSend();
    }

    public Message send(String text, ReplyKeyboard replyMarkup){
        this.text = text;
        this.replyMarkup = replyMarkup;
        return this.executeSend();
    }

    public Message reply(String text){
        return this.reply(text, null, null);
    }

    public Message reply(String text, Boolean allowSendingWithoutReply){
        return this.reply(text, allowSendingWithoutReply, null);
    }

    public Message reply(String text, Boolean allowSendingWithoutReply, ReplyParameters replyParameters){
        this.text = text;
        this.replyToMessageId = message.getMessageId();
        this.allowSendingWithoutReply = allowSendingWithoutReply;
        this.replyParameters = replyParameters;

        return this.executeSend();
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
            logger.error("An error occurred while editing the message", e);
            throw new TelegramApiRuntimeException(e);
        }
    }

    public boolean delete(){
        try {
            return this.sender.execute(DeleteMessage.builder()
                            .chatId(chatId).messageId(message.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            logger.error("An error occurred while deleting the message", e);
            throw new RuntimeException(e);
        }
    }


    private Message executeSend() {
        try {
            Message result = this.sender.execute(SendMessage.builder()
                    .chatId(chatId)
                    .messageThreadId(messageThreadId)
                    .parseMode(entities != null ? null : parseMode)
                    .text(text)
                    .disableNotification(disableNotification)
                    .disableWebPagePreview(disableWebPagePreview)
                    .protectContent(protectContent)
                    .allowSendingWithoutReply(allowSendingWithoutReply)
                    .replyMarkup(replyMarkup)
                    .replyToMessageId(replyToMessageId)
                    .replyParameters(replyParameters)
                    .linkPreviewOptions(linkPreviewOptions)
                    .businessConnectionId(businessConnectionId)
                    .entities(entities)
                    .build());

            if (!sneaky)
                this.memory.setLastSentMessage(result);
            
            sneaky = memory == null;
            return result;
        } catch (TelegramApiException e) {
            logger.error("An error occurred while sending the message", e);
            throw new TelegramApiRuntimeException(e);
        }

    }
}
