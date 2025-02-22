package org.telegrise.telegrise.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.core.SessionMemoryImpl;

import java.util.List;

/**
 * A builder for Telegram message-related API methods.
 * <p>
 * This class extracts all required values for {@code send} and {@code reply} actions
 * and can be configured using builder strategy.
 * It assumes that passed {@link Message} cannot be edited or deleted, so no methods are provided.
 * <pre>
 * {@code
 * sender.of(message).disableNotification(true).send("Hello, World!", replyMarkup);
 * }
 * </pre>
 *
 * @see EditableMessageActionBuilder
 * @since 0.6
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MessageActionBuilder {          //TODO add media support
    private static final Logger logger = LoggerFactory.getLogger(MessageActionBuilder.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageActionBuilder.class);

    protected final TelegramClient sender;

    protected final String chatId;
    protected Integer messageThreadId;
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
    private boolean sneaky = true;

    MessageActionBuilder(BotSender sender, Message message, SessionMemoryImpl memoryImpl) {
        this.sender = sender.getClient();

        this.chatId = String.valueOf(message.getChatId());
        this.replyToMessageId = message.getMessageId();
    }

    MessageActionBuilder(BotSender sender, String chatId, Integer messageId, SessionMemoryImpl memoryImpl) {
        this.sender = sender.getClient();

        this.chatId = chatId;
        this.replyToMessageId = messageId;
    }

    /**
     * Disables marking next sent message at {@link SessionMemory#getLastSentMessage()}.
     * <p>
     * Use this method before the {@code send} or {@code reply} methods.
     * The instance will go back to non-sneaky mode right after the execution of the API method
     * AND DOESN'T set indefinitely.
     * Multiple execution of this method has no effect.
     */
    public MessageActionBuilder sneaky(){
        if (memory == null) 
            logger.warn("Sender is already sneaky since there is no session memory");
            
        sneaky = true;
        return this;
    }

    public MessageActionBuilder messageThreadId(Integer messageThreadId){
        this.messageThreadId = messageThreadId;
        return this;
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

    public Message send(String text) throws TelegramApiException{
        this.text = text;
        this.replyToMessageId = null;
        return this.execute();
    }

    public Message send(String text, ReplyKeyboard replyMarkup) throws TelegramApiException{
        this.text = text;
        this.replyMarkup = replyMarkup;
        this.replyToMessageId = null;
        return this.execute();
    }

    public Message reply(String text) throws TelegramApiException{
        return this.reply(text, null, null);
    }

    public Message reply(String text, Boolean allowSendingWithoutReply, ReplyParameters replyParameters) throws TelegramApiException{
        this.text = text;
        this.allowSendingWithoutReply = allowSendingWithoutReply;
        this.replyParameters = replyParameters;

        return this.execute();
    }

    private Message execute() throws TelegramApiException {
        Message result = this.sender.execute(SendMessage.builder().chatId(chatId).messageThreadId(messageThreadId)
                .parseMode(entities != null ? null : parseMode).text(text).disableNotification(disableNotification)
                .disableWebPagePreview(disableWebPagePreview).protectContent(protectContent)
                .allowSendingWithoutReply(allowSendingWithoutReply).replyMarkup(replyMarkup)
                .replyToMessageId(replyToMessageId).replyParameters(replyParameters)
                .linkPreviewOptions(linkPreviewOptions).businessConnectionId(businessConnectionId).entities(entities)
                .build());

        if (!sneaky)
            this.memory.setLastSentMessage(result);

        sneaky = memory == null;
        return result;
    }
}