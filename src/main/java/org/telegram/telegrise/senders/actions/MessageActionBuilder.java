package org.telegram.telegrise.senders.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.exceptions.TelegramApiRuntimeException;
import org.telegram.telegrise.senders.BotSender;

import java.util.List;

//TODO add media support
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MessageActionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageActionBuilder.class);

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

    public MessageActionBuilder(BotSender sender, Message message) {
        this.message = message;
        this.sender = sender;

        this.chatId = String.valueOf(message.getChatId());
        this.messageThreadId = message.getMessageThreadId();
    }

    public MessageActionBuilder parseMode(String parseMode){
        this.parseMode = parseMode;
        return this;
    }

    public MessageActionBuilder replyParameters(ReplyParameters replyParameters){
        this.replyParameters = replyParameters;
        return this;
    }

    public MessageActionBuilder disableNotification(Boolean disableNotification){
        this.disableNotification = disableNotification;
        return this;
    }

    public MessageActionBuilder entities(List<MessageEntity> entities){
        this.entities = entities;
        return this;
    }

    public MessageActionBuilder replyToMessageId(Integer replyToMessageId){
        this.replyToMessageId = replyToMessageId;
        return this;
    }

    public MessageActionBuilder replyMarkup(ReplyKeyboard replyMarkup){
        this.replyMarkup = replyMarkup;
        return this;
    }

    public MessageActionBuilder allowSendingWithoutReply(Boolean allowSendingWithoutReply){
        this.allowSendingWithoutReply = allowSendingWithoutReply;
        return this;
    }

    public MessageActionBuilder businessConnectionId(String businessConnectionId){
        this.businessConnectionId = businessConnectionId;
        return this;
    }

    public MessageActionBuilder protectContent(Boolean protectContent){
        this.protectContent = protectContent;
        return this;
    }

    public MessageActionBuilder disableWebPagePreview(Boolean disableWebPagePreview){
        this.disableWebPagePreview = disableWebPagePreview;
        return this;
    }

    public MessageActionBuilder linkPreviewOptions(LinkPreviewOptions linkPreviewOptions){
        this.linkPreviewOptions = linkPreviewOptions;
        return this;
    }

    public Message send(String text){
        this.text = text;
        return this.execute();
    }

    public Message send(String text, ReplyKeyboard replyMarkup){
        this.text = text;
        this.replyMarkup = replyMarkup;
        return this.execute();
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

        return this.execute();
    }

    private Message execute() {
        try {
            return this.sender.execute(SendMessage.builder()
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
        } catch (TelegramApiException e) {
            LOGGER.error("An error occurred while sending the message", e);
            throw new TelegramApiRuntimeException(e);
        }
    }
}