package org.telegram.telegrise.senders;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumbnail;
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.senders.actions.CallbackQueryActionBuilder;
import org.telegram.telegrise.senders.actions.EditableMessageActionBuilder;
import org.telegram.telegrise.senders.actions.MessageActionBuilder;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class BotSender {
    private static final Logger logger = LoggerFactory.getLogger(BotSender.class);
    public static final String DEFAULT_PARSE_MODE = "html";

    @Getter
    private final TelegramClient client;
    private final SessionMemoryImpl memory;
    private boolean isSneaky;

    public BotSender(TelegramClient client, SessionMemoryImpl memory) {
        this.client = client;
        this.memory = memory;
        isSneaky = memory == null;
    }

    public boolean delete(MaybeInaccessibleMessage message) throws TelegramApiException {
        return this.client.execute(DeleteMessage.builder().chatId(message.getChatId()).messageId(message.getMessageId()).build());
    }

    public boolean delete(CallbackQuery query) throws TelegramApiException {
        return delete(query.getMessage());
    }

    public MessageActionBuilder of(String chatId){
        return new MessageActionBuilder(this, chatId, null, memory);
    }

    public MessageActionBuilder of(long chatId){
        return new MessageActionBuilder(this, Long.toString(chatId), null, memory);
    }

    public MessageActionBuilder of(Message message){
        return new MessageActionBuilder(this, message, memory);
    }

    public EditableMessageActionBuilder ofEditable(Message message){
        return new EditableMessageActionBuilder(this, message, memory);
    }

    public CallbackQueryActionBuilder of(CallbackQuery callbackQuery) {
        return new CallbackQueryActionBuilder(this, callbackQuery, memory);
    }

    private void finish(@Nullable Message message) {
        if (!isSneaky && message != null && memory != null){
            this.memory.setLastSentMessage(message);
        }

        isSneaky = memory == null;
    }

    public BotSender sneaky(){
        if (memory == null) 
            logger.warn("Sender is already sneaky since there is no session memory");
            
        this.isSneaky = true;
        return this;
    }

    public final java.io.File downloadFile(String filePath) throws TelegramApiException {
        return client.downloadFile(filePath);
    }

    public final java.io.File downloadFile(File file) throws TelegramApiException {
        return client.downloadFile(file);
    }

    public InputStream downloadFileAsStream(File file) throws TelegramApiException{
        return this.client.downloadFileAsStream(file);
    }

    public InputStream downloadFileAsStream(String filePath) throws TelegramApiException {
        return this.client.downloadFileAsStream(filePath);
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) throws TelegramApiException {
        return this.client.executeAsync(method).thenApply(r -> {
            this.finish(r instanceof Message m ? m : null);
            return r;
        });
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        T result = this.client.execute(method);
        this.finish(result instanceof Message m ? m : null);
        return result;
    }

    public final User getMe() throws TelegramApiException {
        this.finish(null);
        return this.client.execute(new GetMe());
    }

    public final WebhookInfo getWebhookInfo() throws TelegramApiException {
        this.finish(null);
        return this.client.execute(new GetWebhookInfo());
    }

    public final CompletableFuture<User> getMeAsync() throws TelegramApiException {
        this.finish(null);
        return this.client.executeAsync(new GetMe());
    }

    public final CompletableFuture<WebhookInfo> getWebhookInfoAsync() throws TelegramApiException {
        this.finish(null);
        return this.client.executeAsync(new GetWebhookInfo());
    }

    public Message execute(SendDocument method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendPhoto method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVideo method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVideoNote method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendSticker method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendAudio method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVoice method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public List<Message> execute(SendMediaGroup method) throws TelegramApiException {
        List<Message> message = this.client.execute(method);
        this.finish(message.get(0));
        return message;
    }

    public Boolean execute(SetChatPhoto method) throws TelegramApiException {
        Boolean b = this.client.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(AddStickerToSet method) throws TelegramApiException {
        Boolean b = this.client.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(SetStickerSetThumbnail method) throws TelegramApiException {
        Boolean b = this.client.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(CreateNewStickerSet method) throws TelegramApiException {
        Boolean b = this.client.execute(method);
        this.finish(null);
        return b;
    }

    public File execute(UploadStickerFile method) throws TelegramApiException {
        File file = this.client.execute(method);
        this.finish(null);
        return file;
    }

    public Serializable execute(EditMessageMedia method) throws TelegramApiException {
        Serializable s = this.client.execute(method);
        this.finish(null);
        return s;
    }

    public Message execute(SendAnimation method) throws TelegramApiException {
        Message message = this.client.execute(method);
        this.finish(message);
        return message;
    }

    public CompletableFuture<Message> executeAsync(SendDocument method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendPhoto method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVideo method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVideoNote method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendSticker method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendAudio method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVoice method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<List<Message>> executeAsync(SendMediaGroup method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(SetChatPhoto method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(AddStickerToSet method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(SetStickerSetThumbnail method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(CreateNewStickerSet method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<File> executeAsync(UploadStickerFile method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Serializable> executeAsync(EditMessageMedia method) {
        return this.client.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendAnimation method) {
        return this.client.executeAsync(method);
    }
}
