package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.*;
import org.telegrise.telegrise.core.caching.MethodReferenceCache;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.Root;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.security.Role;
import org.telegrise.telegrise.core.transition.ExecutionOptions;
import org.telegrise.telegrise.core.transition.TransitionController;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.resources.ResourceInjector;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.UserRole;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.telegrise.telegrise.core.elements.Tree.*;

public class UserSession implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private final ThreadLocal<SessionIdentifier> userIdentifier = new ThreadLocal<>();
    @Getter
    private final SessionMemoryImpl sessionMemory;
    private final BotTranscription transcription;
    private final BotSender sender;
    @Getter
    private final ResourceInjector resourceInjector;
    @Getter
    private final Deque<TreeExecutor> treeExecutors = new ConcurrentLinkedDeque<>();

    private final BlockingQueue<Update> updatesQueue = new LinkedBlockingQueue<>();
    private final TransitionController transitionController;
    private final UpdateHandlersController updateHandlersController;
    private final MediaCollector mediaCollector = new MediaCollector(this.updatesQueue);
    @Getter
    private final TranscriptionManager transcriptionManager;
    @Setter
    private RoleProvider roleProvider;
    private final AtomicBoolean running = new AtomicBoolean();
    private long lastUpdateReceivedAt = 0;

    public UserSession(SessionIdentifier sessionIdentifier, BotTranscription transcription, TelegramClient client, Function<SessionIdentifier, TranscriptionManager> transcriptionGetter) {
        this.userIdentifier.set(sessionIdentifier);
        this.sessionMemory = new SessionMemoryImpl(transcription.hashCode(), sessionIdentifier, transcription.getUsername().generate(new ResourcePool()));
        this.transcription = transcription;
        this.sender = new BotSender(client, sessionMemory);
        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory(), this.sender);
        this.transcriptionManager = new TranscriptionManager(
                this::interruptTreeChain, this::executeBranchingElement, sessionMemory,
                transitionController, transcription, transcriptionGetter, this::createResourcePool
        );
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender, this.sender.getClient(), this.mediaCollector, this.transcriptionManager);
        this.updateHandlersController = new UpdateHandlersController(resourceInjector);
        this.initialize();
    }

    public UserSession(SessionIdentifier sessionIdentifier, SessionMemoryImpl sessionMemory, BotTranscription transcription, TelegramClient client, Function<SessionIdentifier, TranscriptionManager> transcriptionGetter) {
        this.userIdentifier.set(sessionIdentifier);

        if (sessionMemory.getTranscriptionHashcode() == transcription.hashCode()){
            this.sessionMemory = sessionMemory;
            this.transcription = transcription;
        } else
            throw new TelegRiseRuntimeException("Loaded SessionMemory object relates to another bot transcription");

        this.sender = new BotSender(client, sessionMemory);
        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory(), this.sender);
        this.transcriptionManager = new TranscriptionManager(
                this::interruptTreeChain, this::executeBranchingElement, this.sessionMemory,
                transitionController, transcription, transcriptionGetter, this::createResourcePool
        );
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender, this.sender.getClient(), this.mediaCollector, this.transcriptionManager);
        this.updateHandlersController = new UpdateHandlersController(resourceInjector);

        this.initialize();
    }

    private void initialize(){
        this.sessionMemory.getBranchingElements().add(this.transcription.getRoot());
    }

    public void update(Update update){
        if (transcription.getThrottlingTime() != null &&
                transcription.getThrottlingTime() > Math.abs(lastUpdateReceivedAt - System.currentTimeMillis())){
            return;   // Ignores update
        }

        this.updatesQueue.add(update);
        lastUpdateReceivedAt = System.currentTimeMillis();
    }

    public boolean isRunning(){
        return this.running.get();
    }

    @Override
    public void run() {
        if (this.running.get()) return;

        this.running.set(true);

        try {
            while (!this.updatesQueue.isEmpty())
                this.handleUpdate(this.updatesQueue.remove());
        } catch (Throwable e) {
            logger.error("An error occurred running session {}", userIdentifier.get(), e);
            throw TelegRiseRuntimeException.unfold(e);
        } finally {
            this.running.set(false);
        }
    }

    private void handleUpdate(Update update) {
        ResourcePool pool = this.createResourcePool(update);

        if (this.roleProvider != null && this.sessionMemory.getUserRole() == null)
            this.updateRole(update);

        Optional<UpdateHandler> handlerCandidate = this.updateHandlersController.getApplicableHandler(update);
        if (handlerCandidate.isPresent()){
            boolean intercept = this.updateHandlersController.applyHandler(update, handlerCandidate.get());

            if (intercept) return;
        }

        if (this.sessionMemory.isOnRoot())
            this.initializeTree(update, transcription.getRoot());

        else if (this.sessionMemory.isOnStack(Tree.class)) {
            if (this.transcription.isInterruptions()){
                TreeExecutor executor = this.treeExecutors.getLast();
                Optional<Tree> treeCandidate = this.getInterruptionCandidate(update, pool, executor);

                if (treeCandidate.isPresent()){
                    this.interruptTreeChain(update, treeCandidate.get(), true);
                    return;
                }
            }

            this.updateTree(update);
        }
    }

    private Optional<Tree> getInterruptionCandidate(Update update, ResourcePool pool, TreeExecutor executor) {
        List<String> scopes = executor.getCurrentInterruptionScopes();

        if(scopes.contains(INTERRUPT_BY_NONE))
            return Optional.empty();

        Chat chat = MessageUtils.getChat(update);
        boolean containAll = scopes.contains(INTERRUPT_BY_ALL);
        List<String> lastScopes = List.of(this.sessionMemory.getLastChatTypes());
        List<Tree> trees = this.transcription.getRoot().getTrees().stream()
                .filter(t -> t.isChatApplicable(lastScopes, chat))
                .toList();

        if (update.hasCallbackQuery() && (containAll || scopes.contains(INTERRUPT_BY_CALLBACKS))){
            Optional<Tree> candidate = trees.stream()
                    .filter(t -> t.getCallbackTriggers() != null)
                    .filter(t -> List.of(t.getCallbackTriggers()).contains(update.getCallbackQuery().getData()))
                    .findFirst();

            if (candidate.isPresent()) return candidate;
        }

        if (update.hasMessage() && update.getMessage().hasText() && (containAll || scopes.contains(INTERRUPT_BY_COMMANDS))){
            Optional<Tree> candidate = trees.stream()
                    .filter(t -> t.getCommands() != null)
                    .filter(t -> t.isApplicableCommand(update.getMessage().getText(), chat, pool))
                    .findFirst();

            if (candidate.isPresent()) return candidate;
        }

        if (update.hasMessage() && update.getMessage().hasText() && (containAll || scopes.contains(INTERRUPT_BY_KEYS))){
            Optional<Tree> candidate = trees.stream()
                    .filter(t -> t.getKeys() != null)
                    .filter(t -> List.of(t.getKeys()).contains(update.getMessage().getText()))
                    .findFirst();

            if (candidate.isPresent()) return candidate;
        }

        return Optional.empty();
    }

    private void interruptTreeChain(Update update, Tree tree, boolean execute) {
        this.treeExecutors.forEach(TreeExecutor::beforeRemoving);

        this.treeExecutors.clear();
        this.sessionMemory.setCurrentBranch(null);
        this.sessionMemory.getJumpPoints().clear();

        this.sessionMemory.getBranchingElements().clear();
        this.sessionMemory.getBranchingElements().add(this.transcription.getRoot());
        this.initializeTree(update, tree, execute);
    }

    private void initializeTree(Update update, Root root) {
        ResourcePool pool = this.createResourcePool(update);
        Tree tree = root.findTree(this.createResourcePool(update), this.sessionMemory);

        if (tree != null) {
            this.initializeTree(update, tree, true);
            return;
        }

        if (root.getDefaultBranch() != null && root.getDefaultBranch().getWhen().generate(pool)){
            TreeExecutor.invokeBranch(root.getDefaultBranch().getToInvoke(), root.getDefaultBranch().getActions(),
                    pool, sender, ExecutionOptions.always());
        } else {
            Optional<UpdateHandler> handlerCandidate = this.updateHandlersController.getApplicableAfterTreesHandler(update);
            handlerCandidate.ifPresent(primaryHandler -> this.updateHandlersController.applyHandler(update, primaryHandler));
        }
    }

    private void initializeTree(Update update, Tree tree, boolean execute) {
        if (roleProvider != null && update != null && !this.checkForTreeAccessibility(tree, update))
            return;

        this.applyTree(update, tree, execute);
    }

    private void applyTree(Update update, Tree tree, boolean execute) {
        if (tree.getBranches() != null || (tree.getController() != null && execute)) {
            this.sessionMemory.getBranchingElements().add(tree);
            TreeExecutor executor = TreeExecutor.create(tree, this.resourceInjector, this.sender, this.sessionMemory, updatesQueue);
            this.treeExecutors.add(executor);

            try {
                if (execute)
                    this.executeBranchingElement(tree, update);
            } finally {
                if(tree.getBranches() == null)
                    sessionMemory.getBranchingElements().remove(tree);
            }
        } else if(execute) {
            sessionMemory.getBranchingElements().add(tree);
            try {
                this.executeBranchingElement(tree, update);
            } finally {
                sessionMemory.getBranchingElements().remove(tree);
            }
        }
    }

    private boolean checkForTreeAccessibility(Tree tree, Update update){
        Role role = updateRole(update);
        if (role == null) return false;

        if (tree.getAccessLevel() != null && role.getLevel() != null && role.getLevel() >= tree.getAccessLevel())
            return true;
        else if (role.getTrees() != null && Arrays.stream(role.getTrees()).anyMatch(t -> t.equals(tree.getName())))
            return true;

        if (role.getOnDeniedTree() != null){
            Tree onDenied = this.transcription.getMemory().get(role.getOnDeniedTree(), Tree.class, List.of("tree"));
            this.applyTree(update, onDenied, true);
        }

        return false;
    }

    @Nullable
    private Role updateRole(Update update) {
        String roleName = this.roleProvider.getRole(MessageUtils.getFrom(update), this.sessionMemory);
        if(roleName == null){
            this.sessionMemory.setUserRole(null);
            return null;
        }

        Role role = this.transcription.getMemory().get(roleName, Role.class, List.of("role"));
        this.sessionMemory.setUserRole(UserRole.ofRole(role));
        return role;
    }

    private void updateTree(Update update) {
        TreeExecutor executor = this.treeExecutors.getLast();
        ResourcePool pool = this.createResourcePool(update);

        try {
            executor.update(update);
        } finally {
            this.sessionMemory.setCurrentBranch(executor.getCurrentBranch());
        }

        if (executor.isClosed())
            this.processClosedTree(update, executor, pool);
        else
            this.sessionMemory.setCurrentBranch(executor.getCurrentBranch());

        this.updateCaches(pool);
    }

    /** There are four cases when the executor is closed:
     * <ol>
     *      <li>Transition is declared — performs transition</li>
     *      <li>Unrecognized (no branch responded) update, but predicate interrupter found — performs interruption</li>
     *      <li>Unrecognized update, no interrupter — ignores update</li>
     *      <li>Naturally closed (branch or tree has no continuation) — goes back to the previous branch element (and executes)</li>
     * </ol>
     */
    private void processClosedTree(Update update, TreeExecutor executor, ResourcePool pool) {
        ExecutionOptions execute = ExecutionOptions.always();
        if (executor.getLastBranch() != null && executor.getLastBranch().getTransition() != null) {
            // Transition case
            boolean interrupted = this.transitionController.applyTransition(executor.getTree(), executor.getLastBranch().getTransition(), pool);
            execute = executor.getLastBranch().getTransition().getExecutionOptions();

            if (interrupted) return;
        } else if(!executor.isNaturallyClosed()){
            Chat chat = MessageUtils.getChat(update);
            List<String> lastScopes = List.of(this.sessionMemory.getLastChatTypes());
            if ((executor.getCurrentInterruptionScopes().contains(INTERRUPT_BY_ALL)
                || executor.getCurrentInterruptionScopes().contains(INTERRUPT_BY_PREDICATES)))
            {
                for (Tree tree : this.transcription.getRoot().getTrees()) {
                    if (tree.getPredicate() != null && tree.isChatApplicable(lastScopes, chat) && tree.getPredicate().generate(pool)) {
                        // Interrupter found case (tree)
                        this.interruptTreeChain(update, tree, true);
                        return;
                    }
                }
            }

            Optional<UpdateHandler> handlerCandidate = this.updateHandlersController.getApplicableAfterTreesHandler(update);
            // Interrupter found case (handler where Handler.afterTress() is true)
            if (handlerCandidate.isPresent()){
                boolean intercept = this.updateHandlersController.applyHandler(update, handlerCandidate.get());

                if (intercept) return;
            }

            // Unrecognized update case
            executor.open();
            return;
        }

        // Naturally closed case
        // This condition prevents interfering with a transition case
        if (executor.getLastBranch() == null || executor.getLastBranch().getTransition() == null)
            this.transitionController.removeExecutor(executor);
        BranchingElement last = this.sessionMemory.getBranchingElements().getLast();
        if (last instanceof Tree && !this.treeExecutors.getLast().getTree().getName().equals(last.getName()))
            this.treeExecutors.add(TreeExecutor.create((Tree) last, this.resourceInjector, this.sender, this.sessionMemory, updatesQueue));

        this.executeBranchingElement(this.sessionMemory.getBranchingElements().getLast(), update, execute);
    }

    private void updateCaches(ResourcePool pool) {
        for (MethodReferenceCache r : sessionMemory.getCacheMap().values()) {
            if (r.isEmpty()) continue;

            if (!r.isCacheApplicable(pool))
                r.clear();
        }

        if(sessionMemory.getCurrentTree() != null){
            BranchingElement currentElement = Objects.requireNonNullElse(sessionMemory.getCurrentBranch(), sessionMemory.getCurrentTree());
            sessionMemory.getKeyboardStates().entrySet().removeIf(e -> !e.getValue().getParent().equals(currentElement)
                        && e.getValue().getParent().getLevel() > currentElement.getLevel());
        } else {
            sessionMemory.getKeyboardStates().clear();
        }
    }

    private ResourcePool createResourcePool(Update update) {
        return new ResourcePool(
                update,
                this.treeExecutors.isEmpty() ? null : this.treeExecutors.getLast().getControllerInstance(),
                this.sender,
                this.sessionMemory,
                this.treeExecutors.peekLast(),
                this.updatesQueue
        );
    }

    private void executeBranchingElement(BranchingElement element, Update update){
        executeBranchingElement(element, update, ExecutionOptions.always());
    }

    private void executeBranchingElement(BranchingElement element, Update update, ExecutionOptions options){
        if (element.getActions() == null) return;
        TreeExecutor.invokeBranch(null, element.getActions(), this.createResourcePool(update), sender, options);
    }

    public void addHandlersClasses(List<Class<? extends UpdateHandler>> classes){
        classes.forEach(this.updateHandlersController::add);
    }

    public void setStandardLanguage(String code){
        this.sessionMemory.setLanguageCode(code);
    }

    @FunctionalInterface
    public interface TranscriptionInterrupter {
        void transit(Update update, Tree tree, boolean execute);
    }
}
