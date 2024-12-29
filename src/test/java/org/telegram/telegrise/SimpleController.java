package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.annotations.Handler;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.Resource;
import org.telegram.telegrise.annotations.TreeController;
import org.telegram.telegrise.core.builtin.DefaultController;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.senders.BotSender;
import org.telegram.telegrise.utils.MessageUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@TreeController
public class SimpleController extends DefaultController {
    @Reference
    public static boolean isHello(Update update, SessionMemory memory){
        if (!memory.containsKey("hello", memory.getCurrentTree()))
            memory.put("hello", memory.getCurrentTree(), false);

        memory.put("hello", memory.getCurrentTree(), !memory.get("hello", memory.getCurrentTree(), Boolean.class));
        return memory.get("hello", memory.getCurrentTree(), Boolean.class);
    }

    @Reference
    public static boolean checkStart(Update update, SessionMemory memory){
        return update.hasMessage();
    }

    @Reference
    public Consumer<Update> getWarnListener(){
        return update -> {
            try {
                this.sender.of(memory.getLastSentMessage()).send("You are not allowed to use this command");
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }

    @Reference
    public static boolean getTrue(Update update){
        return true;
    }

    @Reference
    public String concat(String... strings){
        return String.join(" ", strings);
    }

    @Reference
    public static void printHello(){
        System.out.println("Hello for static method!");
    }

    @Resource
    private BotSender sender;

    @Resource
    private SessionMemory memory;

    @Resource
    private MediaCollector mediaCollector;
    @Resource
    private TranscriptionManager objectManager;

    public static void main(String[] args) {
        TelegRiseApplication application = new TelegRiseApplication(new File("src/test/resources/index.xml"), SimpleController.class);
//        application.setExecutorService(Executors::newSingleThreadExecutor);
        application.setRoleProvider(new Provider());
        application.setSessionInitializer(new SessionInitializer() {
            @Resource
            private BotSender sender;

            @Override
            public void initialize(SessionMemory memory) {
                memory.put("test", "test");
                try {
                    sender.of(memory.getChatId()).reply("Initialized " + memory.getSessionIdentifier());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        application.addService(new SimpleService());
        application.start();
    }

    @Reference
    public boolean predicate(Update update){
        return update.hasMessage() && update.getMessage().hasPhoto();
    }

    @Reference
    public void log(Update update){
        System.out.println("Got a message from " + Objects.requireNonNull(MessageUtils.getFrom(update)).getFirstName());
        System.out.println("Last sent message ID: " + Objects.requireNonNull(this.memory.getLastSentMessage()).getMessageId());
        System.out.println("Start text: " + this.objectManager.getTextBlock("startText").getText(update).replace("\n", "   "));
    }

    @Reference
    public boolean not(boolean b){
        return !b;
    }

    @SuppressWarnings("DataFlowIssue")
    @Reference
    public void send(Update update) throws TelegramApiException, InterruptedException {
        System.out.println(memory.getLastSentMessage().getMessageId());
        Message m = sender.of(memory.getChatId()).send("Done");
        System.out.println(memory.getLastSentMessage().getMessageId());
        Thread.sleep(1000);
        sender.ofEditable(m).edit(m.getText() +"... or is it?");
    }

    public static InputStream downloadFile(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to download file: " + connection.getResponseMessage());
        }

        return connection.getInputStream();
    }

    @Reference
    public InputFile getVideo() throws Exception {
        return new InputFile(downloadFile("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"), "video.mp4");
    }

    @Reference
    public void switchButton(){
        this.memory.getKeyboardState("custom", memory.getCurrentTree())
            .flipSwitch("switch");
    }

    @Reference
    public void disableButton(){
        this.memory.getKeyboardState("custom", memory.getCurrentTree()).disableRow(2);
    }

    @Reference
    public boolean doSendPhoto(Update update){
        return List.of(503138767L, 5435941198L).contains(Objects.requireNonNull(MessageUtils.getFrom(update)).getId());
    }

    @Reference
    public boolean isMediagroup(Update update){
        return update.hasMessage() && update.getMessage().getMediaGroupId() != null;
    }

    @Reference
    public List<InputMedia> duplicateMedias(Update update){
        return this.mediaCollector.collect(update).stream().map(MessageUtils::toInputMedia).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public boolean check(){
        System.out.println("Check executed");
        return true;
    }

    @Reference
    public PartialBotApiMethod<?> createSendMethod(Send send, Update update){
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Got Send object: " + send.toString())
                .build();
    }

    @Reference
    public void consumeReturn(Message result){
        System.out.println("Returned: " + result.getText());
    }

    @TreeController
    public static class ChildController extends SimpleController{
        @Resource
        private SessionMemory memory;
    }

    @Handler(absolute = true)
    public static class PHandler implements PrimaryHandler {
        @Resource
        private BotSender sender;
        @Resource
        private TranscriptionManager manager;

        @Override
        public boolean canHandle(Update update) {
            return update.hasMessage() && "/transit".equals(update.getMessage().getText());
        }

        @Override
        public void handle(Update update) throws TelegramApiException {
            sender.of(update.getMessage()).send("Primary handler triggered from tree " + manager.getCurrentTree());
            manager.transitBack(update, "root", true);
        }
    }

    @Handler(absolute = true)
    public static class SHandler implements PrimaryHandler {
        @Resource
        private SessionMemory memory;
        @Resource
        private TranscriptionManager manager;

        @Override
        public boolean canHandle(Update update) {
            return update.hasMessage() && "/reload".equals(update.getMessage().getText());
        }

        @Override
        public void handle(Update update) {
            manager.reinitializeSession(memory.getSessionIdentifier());
        }
    }

    @Handler(independent = true, absolute = true)
    public static class SecondHandler implements PrimaryHandler {
        @Override
        public boolean canHandle(Update update) {
            return update.hasMessage() && "/second".equals(update.getMessage().getText());
        }

        @Override
        public void handle(Update update) {
            System.out.println("Second primary handler triggered from " + Objects.requireNonNull(MessageUtils.getChat(update)).getId());
        }
    }

    public static class Provider implements RoleProvider{

        @Override
        public String getRole(User user, SessionMemory sessionMemory) {
            return List.of(503138767L, 5435941198L).contains(user.getId()) ? "admin"
                    : user.getId() == 5128967123L ? "secondary" : "unauthorised";
        }
    }

    @TreeController
    public static class StartController{
        private boolean hello;

        @Reference
        public boolean isHello(){
            this.hello = !hello;
            return this.hello;
        }
    }

    public static class SimpleService implements Service{
        @Resource
        private BotSender sender;

        @Override
        public void run() {
            System.out.println("Service started with injected: " + sender);
        }
    }
}