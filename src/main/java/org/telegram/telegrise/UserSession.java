package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.Menu;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.elements.security.Role;
import org.telegram.telegrise.resources.ResourceInjector;
import org.telegram.telegrise.transition.TransitionController;
import org.telegram.telegrise.types.UserRole;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.telegram.telegrise.core.elements.Tree.*;

public class UserSession implements Runnable{
    private final ThreadLocal<UserIdentifier> userIdentifier = new ThreadLocal<>();
    @Getter
    private final SessionMemoryImpl sessionMemory;
    private final BotTranscription transcription;
    private final DefaultAbsSender sender;
    @Getter
    private final ResourceInjector resourceInjector;
    @Getter
    private final Deque<TreeExecutor> treeExecutors = new ConcurrentLinkedDeque<>();

    private final BlockingQueue<Update> updatesQueue = new LinkedBlockingQueue<>();
    private final TransitionController transitionController;
    private final PrimaryHandlersController primaryHandlersController;
    private final MediaCollector mediaCollector = new MediaCollector(this.updatesQueue);
    private final UniversalSender universalSender;
    private final TranscriptionManager transcriptionManager;
    @Setter
    private RoleProvider roleProvider;
    private final AtomicBoolean running = new AtomicBoolean();

    public UserSession(UserIdentifier userIdentifier, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier.set(userIdentifier);
        this.sessionMemory = new SessionMemoryImpl(transcription.hashCode(), userIdentifier, transcription.getUsername());
        this.transcription = transcription;
        this.sender = sender;
        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory(), sender);
        this.transcriptionManager = new TranscriptionManager(this::interruptTreeChain, this::executeBranchingElement, sessionMemory, transitionController, this::createResourcePool);
        this.transcriptionManager.load(transcription);
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender, this.mediaCollector, this.transcriptionManager);
        this.primaryHandlersController = new PrimaryHandlersController(resourceInjector);
        this.initialize();
        this.universalSender = new UniversalSender(sender);
    }

    public UserSession(UserIdentifier userIdentifier, SessionMemoryImpl sessionMemory, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier.set(userIdentifier);
        this.sender = sender;

        if (sessionMemory.getTranscriptionHashcode() == transcription.hashCode()){
            this.sessionMemory = sessionMemory;
            this.transcription = transcription;
        } else
            throw new TelegRiseRuntimeException("Loaded SessionMemory object relates to another bot transcription");

        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory(), sender);
        this.transcriptionManager = new TranscriptionManager(this::interruptTreeChain, this::executeBranchingElement, this.sessionMemory, transitionController, this::createResourcePool);
        this.transcriptionManager.load(transcription);
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender, this.mediaCollector, this.transcriptionManager);
        this.primaryHandlersController = new PrimaryHandlersController(resourceInjector);

        this.initialize();
        this.universalSender = new UniversalSender(sender);
    }

    private void initialize(){
        this.sessionMemory.getBranchingElements().add(this.transcription.getRootMenu());
    }

    public void update(Update update){
        this.updatesQueue.add(update);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.running.set(false);
        }
    }

    private void handleUpdate(Update update) {
        ResourcePool pool = this.createResourcePool(update);

        if (this.roleProvider != null && this.sessionMemory.getUserRole() == null)
            this.updateRole(update);

        Optional<PrimaryHandler> handlerCandidate = this.primaryHandlersController.getApplicableHandler(update);
        if (handlerCandidate.isPresent()){
            boolean intercept = this.primaryHandlersController.applyHandler(update, handlerCandidate.get());

            if (intercept) return;
        }

        if (this.sessionMemory.isOnStack(Menu.class))
            this.initializeTree(update, this.sessionMemory.getFromStack(Menu.class));

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
        List<Tree> trees = this.transcription.getRootMenu().getTrees().stream()
                .filter(t -> t.isChatApplicable(lastScopes, chat))
                .collect(Collectors.toList());

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
        this.sessionMemory.getCurrentBranch().set(null);
        this.sessionMemory.getJumpPoints().clear();

        this.sessionMemory.getBranchingElements().clear();
        this.sessionMemory.getBranchingElements().add(this.transcription.getRootMenu());
        this.initializeTree(update, tree, execute);
    }

    private void initializeTree(Update update, Menu menu) {
        ResourcePool pool = this.createResourcePool(update);
        Tree tree = menu.findTree(this.createResourcePool(update), this.sessionMemory);

        if (tree != null) {
            this.initializeTree(update, tree, true);
            return;
        }

        if (menu.getDefaultBranch() != null && menu.getDefaultBranch().getWhen().generate(pool)){
            TreeExecutor.invokeBranch(menu.getDefaultBranch().getToInvoke(), menu.getDefaultBranch().getActions(),
                    pool, sender);
        }
    }

    private void initializeTree(Update update, Tree tree, boolean execute) {
        if (roleProvider != null && update != null && !this.checkForTreeAccessibility(tree, update))
            return;

        this.applyTree(update, tree, execute);
    }

    private void applyTree(Update update, Tree tree, boolean execute) {
        if (tree.getBranches() != null) {
            TreeExecutor executor = TreeExecutor.create(tree, this.resourceInjector, this.sender, this.sessionMemory, updatesQueue);
            this.treeExecutors.add(executor);
            this.sessionMemory.getBranchingElements().add(tree);
        }

        if (execute)
            this.executeBranchingElement(tree, update);
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

        executor.update(update);
        this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());

        // There are 4 cases when executor is closed:
        // 1. Transition is declared — performs transition
        // 2. Unrecognized (no branch responded) update, but predicate interrupter found — performs interruption
        // 3. Unrecognized update, no interrupter — ignores update (TODO: sends warning)
        // 4. Naturally closed (branch or tree has no continuation) — goes back to the previous branch element (and executes)
        if (executor.isClosed()){
            boolean execute = true;
            if (executor.getLastBranch() != null && executor.getLastBranch().getTransition() != null) {
                // Transition case
                boolean interrupted = this.transitionController.applyTransition(executor.getTree(), executor.getLastBranch().getTransition(), pool);
                execute = executor.getLastBranch().getTransition().isExecute();

                if (interrupted) return;
            } else if(!executor.isNaturallyClosed()){
                Chat chat = MessageUtils.getChat(update);
                List<String> lastScopes = List.of(this.sessionMemory.getLastChatTypes());
                if ((executor.getCurrentInterruptionScopes().contains(INTERRUPT_BY_ALL)
                    || executor.getCurrentInterruptionScopes().contains(INTERRUPT_BY_PREDICATES)))
                {
                    for (Tree tree : this.transcription.getRootMenu().getTrees()) {
                        if (tree.getPredicate() != null && tree.isChatApplicable(lastScopes, chat) && tree.getPredicate().generate(pool)) {
                            // Interrupter found case
                            this.interruptTreeChain(update, tree, true);
                            return;
                        }
                    }
                }

                // Unrecognized update case
                executor.open();
                return;
            }

            // Naturally closed case
            if (executor.getLastBranch() == null || executor.getLastBranch().getTransition() == null)  // This condition prevents interfering with transition case
                this.transitionController.removeExecutor(executor);
            BranchingElement last = this.sessionMemory.getBranchingElements().getLast();
            if (last instanceof Tree && !this.treeExecutors.getLast().getTree().getName().equals(last.getName()))
                this.treeExecutors.add(TreeExecutor.create((Tree) last, this.resourceInjector, this.sender, this.sessionMemory, updatesQueue));

            if (execute)
                this.executeBranchingElement(this.sessionMemory.getBranchingElements().getLast(), update);
        } else {
            this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());
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
        if (element.getActions() == null) return;

        for (ActionElement actionElement : element.getActions()) {
            try {
                this.universalSender.execute(actionElement, this.createResourcePool(update));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addHandlersClasses(List<Class<? extends PrimaryHandler>> classes){
        classes.forEach(this.primaryHandlersController::add);
    }

    @FunctionalInterface
    public interface TranscriptionInterrupter {
        void transit(Update update, Tree tree, boolean execute);
    }
}
