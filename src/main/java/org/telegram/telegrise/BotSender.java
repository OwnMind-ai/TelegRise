package org.telegram.telegrise;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumb;
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.DownloadFileCallback;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class BotSender {
    private final DefaultAbsSender sender;
    private final SessionMemoryImpl memory;
    private boolean isSneaky;

    public BotSender(DefaultAbsSender sender, SessionMemoryImpl memory) {
        this.sender = sender;
        this.memory = memory;
    }

    private void finish(@Nullable Message message) {
        if (!isSneaky && message != null && memory != null){
            this.memory.setLastSentMessage(message);
        }

        isSneaky = false;
    }

    public BotSender sneaky(){
        this.isSneaky = true;
        return this;
    }

    public final java.io.File downloadFile(String filePath) throws TelegramApiException {
        return sender.downloadFile(filePath);
    }

    public final java.io.File downloadFile(File file) throws TelegramApiException {
        return sender.downloadFile(file);
    }

    public final java.io.File downloadFile(File file, java.io.File outputFile) throws TelegramApiException {
        return sender.downloadFile(file, outputFile);
    }

    public final java.io.File downloadFile(String filePath, java.io.File outputFile) throws TelegramApiException {
        return sender.downloadFile(filePath, outputFile);
    }

    public final void downloadFileAsync(String filePath, DownloadFileCallback<String> callback) throws TelegramApiException {
        sender.downloadFileAsync(filePath, callback);
    }

    public final void downloadFileAsync(File file, DownloadFileCallback<File> callback) throws TelegramApiException {
        sender.downloadFileAsync(file, callback);
    }

    public final InputStream downloadFileAsStream(String filePath) throws TelegramApiException {
        return sender.downloadFileAsStream(filePath);
    }

    public final InputStream downloadFileAsStream(File file) throws TelegramApiException {
        return sender.downloadFileAsStream(file);
    }

    public <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void executeAsync(Method method, Callback callback) throws TelegramApiException {
        this.sender.executeAsync(method, callback);
        this.finish(null);
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) throws TelegramApiException {
        CompletableFuture<T> future = this.sender.executeAsync(method);
        this.finish(null);
        return future;
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        T result = this.sender.execute(method);
        this.finish(null);
        return result;
    }

    public final User getMe() throws TelegramApiException {
        this.finish(null);
        return this.sender.getMe();
    }

    public final WebhookInfo getWebhookInfo() throws TelegramApiException {
        this.finish(null);
        return this.sender.getWebhookInfo();
    }

    public final CompletableFuture<User> getMeAsync() {
        this.finish(null);
        return this.sender.getMeAsync();
    }

    public final CompletableFuture<WebhookInfo> getWebhookInfoAsync() {
        this.finish(null);
        return this.sender.getWebhookInfoAsync();
    }

    public final void getMeAsync(SentCallback<User> sentCallback) throws TelegramApiException {
        this.finish(null);
        this.sender.getMeAsync(sentCallback);
    }

    public final void getWebhookInfoAsync(SentCallback<WebhookInfo> sentCallback) throws TelegramApiException {
        this.finish(null);
        this.sender.getWebhookInfoAsync(sentCallback);
    }

    public Message execute(SendDocument method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendPhoto method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVideo method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVideoNote method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendSticker method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendAudio method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public Message execute(SendVoice method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public List<Message> execute(SendMediaGroup method) throws TelegramApiException {
        List<Message> message = this.sender.execute(method);
        this.finish(message.get(0));
        return message;
    }

    public Boolean execute(SetChatPhoto method) throws TelegramApiException {
        Boolean b = this.sender.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(AddStickerToSet method) throws TelegramApiException {
        Boolean b = this.sender.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(SetStickerSetThumb method) throws TelegramApiException {
        Boolean b = this.sender.execute(method);
        this.finish(null);
        return b;
    }

    public Boolean execute(CreateNewStickerSet method) throws TelegramApiException {
        Boolean b = this.sender.execute(method);
        this.finish(null);
        return b;
    }

    public File execute(UploadStickerFile method) throws TelegramApiException {
        File file = this.sender.execute(method);
        this.finish(null);
        return file;
    }

    public Serializable execute(EditMessageMedia method) throws TelegramApiException {
        Serializable s = this.sender.execute(method);
        this.finish(null);
        return s;
    }

    public Message execute(SendAnimation method) throws TelegramApiException {
        Message message = this.sender.execute(method);
        this.finish(message);
        return message;
    }

    public CompletableFuture<Message> executeAsync(SendDocument method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendPhoto method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVideo method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVideoNote method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendSticker method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendAudio method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendVoice method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<List<Message>> executeAsync(SendMediaGroup method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(SetChatPhoto method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(AddStickerToSet method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(SetStickerSetThumb method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Boolean> executeAsync(CreateNewStickerSet method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<File> executeAsync(UploadStickerFile method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Serializable> executeAsync(EditMessageMedia method) {
        return this.sender.executeAsync(method);
    }

    public CompletableFuture<Message> executeAsync(SendAnimation method) {
        return this.sender.executeAsync(method);
    }

    public DefaultAbsSender getClient() {
        return this.sender;
    }
}
