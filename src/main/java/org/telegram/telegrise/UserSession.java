package org.telegram.telegrise;

import lombok.Getter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.Menu;
import org.telegram.telegrise.core.elements.Tree;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserSession implements Runnable{
    private final ThreadLocal<UserIdentifier> userIdentifier = new ThreadLocal<>();
    private final SessionMemoryImpl sessionMemory;
    private final BotTranscription transcription;
    private final DefaultAbsSender sender;  //FIXME
    private final ResourceInjector resourceInjector;
    @Getter
    private final Deque<TreeExecutor> treeExecutors = new ConcurrentLinkedDeque<>();

    private final Queue<Update> updateQueue = new ConcurrentLinkedQueue<>();
    @Getter
    private boolean running;

    public UserSession(UserIdentifier userIdentifier, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier.set(userIdentifier);
        this.sessionMemory = new SessionMemoryImpl(transcription.hashCode(), userIdentifier);
        this.transcription = transcription;
        this.sender = sender;
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender);
        this.initialize();
    }

    public UserSession(UserIdentifier userIdentifier, SessionMemoryImpl sessionMemory, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier.set(userIdentifier);
        this.sender = sender;

        if (sessionMemory.getTranscriptionHashcode() == transcription.hashCode()){
            this.sessionMemory = sessionMemory;
            this.transcription = transcription;
        } else
            throw new TelegRiseRuntimeException("Loaded SessionMemory object relates to another bot transcription");

        this.resourceInjector = new ResourceInjector(this.sessionMemory);
        this.initialize();
    }

    private void initialize(){
        this.sessionMemory.getBranchingElements().add(this.transcription.getRootMenu());
    }

    public void update(Update update){
        this.updateQueue.add(update);
    }

    @Override
    public void run() {
        try {
            this.running = true;

            while (!this.updateQueue.isEmpty())
                this.handleUpdate(this.updateQueue.remove());
        } finally {
            this.running = false;
        }
    }

    private void handleUpdate(Update update) {
        if (this.sessionMemory.isOnStack(Menu.class))
            this.initializeTree(update, this.sessionMemory.getFromStack(Menu.class));
        else if (this.sessionMemory.isOnStack(Tree.class))
            this.updateTree(update);
    }

    private void initializeTree(Update update, Menu menu) {
        Tree tree = menu.findTree(this.createResourcePool(update));

        if (tree != null){
            TreeExecutor executor = TreeExecutor.create(tree, this.resourceInjector, this.sender);
            this.treeExecutors.add(executor);
            this.sessionMemory.getBranchingElements().add(tree);

            this.executeBranchingElement(tree, update);
        }
    }

    private void updateTree(Update update) {
        TreeExecutor executor = this.treeExecutors.getLast();

        executor.update(update);
        this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());

        if (executor.isClosed()){
            this.treeExecutors.remove(executor);
            this.sessionMemory.getCurrentBranch().set(null);

            assert this.sessionMemory.getBranchingElements().getLast().equals(executor.getTree());
            this.sessionMemory.getBranchingElements().removeLast();

            assert this.sessionMemory.getBranchingElements() instanceof Menu;
            this.executeBranchingElement(this.sessionMemory.getBranchingElements().getLast(), update);
        } else {
            this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());
        }
    }

    private ResourcePool createResourcePool(Update update) {
        return new ResourcePool(
                update,
                this.treeExecutors.isEmpty() ? null : this.treeExecutors.getLast().getHandlerInstance()
        );
    }

    private void executeBranchingElement(BranchingElement element, Update update){
        for (PartialBotApiMethod<?> botApiMethod : element.getMethods(this.createResourcePool(update))) {
            try {
                UniversalSender.execute(sender, botApiMethod, null);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
