package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.elements.security.Role;
import org.telegram.telegrise.transition.TransitionController;
import org.telegram.telegrise.types.UserRole;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserSession implements Runnable{
    private final ThreadLocal<UserIdentifier> userIdentifier = new ThreadLocal<>();
    private final SessionMemoryImpl sessionMemory;
    private final BotTranscription transcription;
    private final DefaultAbsSender sender;
    private final ResourceInjector resourceInjector;
    @Getter
    private final Deque<TreeExecutor> treeExecutors = new ConcurrentLinkedDeque<>();

    private final Queue<Update> updateQueue = new ConcurrentLinkedQueue<>();
    private final TransitionController transitionController;
    private final PrimaryHandlersController primaryHandlersController;
    @Setter
    private RoleProvider roleProvider;
    @Getter
    private boolean running;

    public UserSession(UserIdentifier userIdentifier, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier.set(userIdentifier);
        this.sessionMemory = new SessionMemoryImpl(transcription.hashCode(), userIdentifier);
        this.transcription = transcription;
        this.sender = sender;
        this.resourceInjector = new ResourceInjector(this.sessionMemory, this.sender);
        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory());
        this.primaryHandlersController = new PrimaryHandlersController(resourceInjector);
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
        this.transitionController = new TransitionController(this.sessionMemory, treeExecutors, transcription.getMemory());
        this.primaryHandlersController = new PrimaryHandlersController(resourceInjector);
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
        Optional<PrimaryHandler> candidate = this.primaryHandlersController.getApplicableHandler(update);
        if (candidate.isPresent()){
            boolean intercept = this.primaryHandlersController.applyHandler(update, candidate.get());

            if (intercept) return;
        }

        if (this.sessionMemory.isOnStack(Menu.class))
            this.initializeTree(update, this.sessionMemory.getFromStack(Menu.class));
        else if (this.sessionMemory.isOnStack(Tree.class))
            this.updateTree(update);
    }

    private void initializeTree(Update update, Menu menu) {
        Tree tree = menu.findTree(this.createResourcePool(update), this.sessionMemory);

        if (tree != null){
            if (roleProvider != null && !this.checkForTreeAccessibility(tree, update))
                return;

            this.applyTree(update, tree);
        } else if (menu.getDefaultBranch() != null){
            TreeExecutor.invokeBranch(menu.getDefaultBranch().getToInvoke(), menu.getDefaultBranch().getActions(),
                    this.createResourcePool(update), sender);
        }
    }

    private void applyTree(Update update, Tree tree) {
        if (tree.getBranches() != null) {
            TreeExecutor executor = TreeExecutor.create(tree, this.resourceInjector, this.sender, this.sessionMemory);
            this.treeExecutors.add(executor);
            this.sessionMemory.getBranchingElements().add(tree);
        }

        this.executeBranchingElement(tree, update);
    }

    private boolean checkForTreeAccessibility(Tree tree, Update update){
        String roleName = this.roleProvider.getRole(MessageUtils.getFrom(update), this.sessionMemory);
        if(roleName == null){
            this.sessionMemory.setUserRole(null);
            return false;
        }

        Role role = this.transcription.getMemory().get(roleName, Role.class, List.of("role"));
        this.sessionMemory.setUserRole(UserRole.ofRole(role));

        if (tree.getAccessLevel() != null && role.getLevel() != null && role.getLevel() >= tree.getAccessLevel())
            return true;
        else if (role.getTrees() != null && Arrays.stream(role.getTrees()).anyMatch(t -> t.equals(tree.getName())))
            return true;

        if (role.getOnDeniedTree() != null){
            Tree onDenied = this.transcription.getMemory().get(role.getOnDeniedTree(), Tree.class, List.of("tree"));
            this.applyTree(update, onDenied);
        }

        return false;
    }

    private void updateTree(Update update) {
        TreeExecutor executor = this.treeExecutors.getLast();
        ResourcePool pool = this.createResourcePool(update);

        executor.update(update);
        this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());

        if (executor.isClosed()){
            if (executor.getLastBranch().getRefresh() != null){
                Refresh refresh = executor.getLastBranch().getRefresh();
                this.transitionController.applyUpdate(executor.getTree(), refresh, pool);

                if (refresh.isExecute() && refresh.isTransit())
                    this.executeBranchingElement(executor.getTree(), update);
            }

            if (executor.getLastBranch().getTransition() != null) {
                boolean interrupted = this.transitionController.applyTransition(executor.getTree(), executor.getLastBranch().getTransition(), pool);
                executor.clearLastBranch();

                if (interrupted) return;
            } else
                this.transitionController.removeExecutor(executor);

            BranchingElement last = this.sessionMemory.getBranchingElements().getLast();
            if (last instanceof Tree && !this.treeExecutors.getLast().getTree().getName().equals(last.getName()))
                this.treeExecutors.add(TreeExecutor.create((Tree) last, this.resourceInjector, this.sender, this.sessionMemory));

            this.executeBranchingElement(this.sessionMemory.getBranchingElements().getLast(), update);
        } else {
            this.sessionMemory.getCurrentBranch().set(executor.getCurrentBranch());
        }
    }

    private ResourcePool createResourcePool(Update update) {
        return new ResourcePool(
                update,
                this.treeExecutors.isEmpty() ? null : this.treeExecutors.getLast().getHandlerInstance(),
                this.sender,
                this.sessionMemory
        );
    }

    private void executeBranchingElement(BranchingElement element, Update update){
        for (ActionElement actionElement : element.getActions()) {
            try {
                UniversalSender.execute(sender, actionElement, this.createResourcePool(update));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addHandlersClasses(List<Class<? extends PrimaryHandler>> classes){
        classes.forEach(this.primaryHandlersController::add);
    }
}
