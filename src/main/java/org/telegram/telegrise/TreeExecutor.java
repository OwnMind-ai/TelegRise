package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.annotations.OnClose;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.DefaultBranch;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.elements.actions.Edit;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.resources.ResourceInjector;
import org.telegram.telegrise.senders.BotSender;
import org.telegram.telegrise.senders.UniversalSender;
import org.telegram.telegrise.transition.ExecutionOptions;
import org.telegram.telegrise.utils.MessageUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public final class TreeExecutor {
    public static TreeExecutor create(Tree tree, ResourceInjector resourceInjector, BotSender sender, SessionMemoryImpl memory, BlockingQueue<Update> updatesQueue) {
        Object handler = null;
        if (tree.getController() != null)
            handler = new TreeControllerInitializer(tree.getController(), resourceInjector).initialize();

        return new TreeExecutor(memory, handler, tree, sender, updatesQueue);
    }

    public static void invokeBranch(GeneratedValue<Void> toInvoke, List<ActionElement> actions, ResourcePool pool, BotSender sender, ExecutionOptions options){
        if (options.execute() != null && !options.execute()) return;

        if (options.execute() != null) {
            if (toInvoke != null) toInvoke.generate(pool);
            if (actions == null) return;

            actions.forEach(action -> {
                try {
                    new UniversalSender(sender).execute(action, pool);
                } catch (TelegramApiException e) {
                    throw new TelegRiseInternalException(e);
                }
            });
        } else if (options.edit() != null && actions != null) {
            UniversalSender universalSender = new UniversalSender(sender);
            boolean isFirst = options.edit().equals(ExecutionOptions.EDIT_FIRST);
            for (ActionElement action : actions) {
                try {
                    Edit editAction = action.toEdit();
                    if (isFirst && editAction != null) {
                        universalSender.execute(editAction, pool);
                        return;
                    } else if (!isFirst && options.edit().equals(action.getName())) {
                        if(editAction == null)
                            throw new TelegRiseRuntimeException("Unable to convert element named '%s' to edit action".formatted(options.edit()), action.getElementNode());
                        universalSender.execute(editAction, pool);
                        return;
                    }
                } catch (TelegramApiException e) {
                    throw new TelegRiseInternalException(e);
                }
            }
            throw new TelegRiseRuntimeException("Unable to find an element to edit after the transition", actions.get(0).getElementNode());
        }
    }

    private final SessionMemoryImpl memory;

    @Getter
    private final Object controllerInstance;
    @Getter
    private final Tree tree;
    @Getter
    private final BotSender sender;
    @Getter
    private final BlockingQueue<Update> updatesQueue;
    @Getter @Setter
    private Branch currentBranch;
    @Getter
    private boolean closed;
    @Getter
    private boolean naturallyClosed;

    @Getter
    private Branch lastBranch;

    public TreeExecutor(SessionMemoryImpl memory, Object controllerInstance, Tree tree, BotSender sender, BlockingQueue<Update> updatesQueue) {
        this.memory = memory;
        this.controllerInstance = controllerInstance;
        this.tree = tree;
        this.sender = sender;
        this.updatesQueue = updatesQueue;
    }

    public void update(Update update){
        this.closed = false;
        List<Branch> nextBranches = currentBranch != null ? currentBranch.getBranches() : tree.getBranches();
        ResourcePool resourcePool = new ResourcePool(update, controllerInstance, this.sender, this.memory, this, updatesQueue);

        Branch previous = this.currentBranch;
        this.currentBranch = this.getNextBranch(nextBranches, resourcePool);

        // Next branch found
        if (this.currentBranch != null){
            this.memory.setCurrentBranch(this.currentBranch);
            try {
                this.invokeBranch(this.currentBranch.getToInvoke(), this.currentBranch.getActions(), resourcePool);
            } finally {
                if (this.currentBranch == null) {
                    this.lastBranch = previous;
                    this.naturallyClosed = false;
                    this.close();
                } else if (this.currentBranch.getBranches() == null || this.currentBranch.getBranches().isEmpty()) {
                    this.lastBranch = this.currentBranch;
                    this.naturallyClosed = true;
                    this.close();
                }
            }
        } else if (nextBranches == null || nextBranches.isEmpty()) {       // There is no continuation of the branch
            this.lastBranch = previous;
            this.naturallyClosed = false;
            this.close();
        } else if(previous != null && previous.getDefaultBranch() != null) {   // Branch wasn't found, looking for default one in previous branch
            DefaultBranch defaultBranch = previous.getDefaultBranch();
            this.currentBranch = previous;

            if (defaultBranch.getWhen().generate(resourcePool))
                this.invokeBranch(defaultBranch.getToInvoke(), defaultBranch.getActions(), resourcePool);
        } else if(previous == null && this.tree.getDefaultBranch() != null) {  // the branch wasn't found, looking for the default one in the tree
            if (this.tree.getDefaultBranch().getWhen().generate(resourcePool))
                this.invokeBranch(this.tree.getDefaultBranch().getToInvoke(), this.tree.getDefaultBranch().getActions(), resourcePool);
        } else {
            this.currentBranch = previous;      // Effectively ignores the update
        }
    }

    private void invokeBranch(GeneratedValue<Void> toInvoke, List<ActionElement> actions, ResourcePool pool){
        invokeBranch(toInvoke, actions, pool, this.sender, ExecutionOptions.always());
    }

    public List<String> getCurrentInterruptionScopes(){
        return !hasInterruptions(this.currentBranch) && !hasInterruptions(this.lastBranch) ? List.of(this.tree.getAllowedInterruptions())
                : hasInterruptions(this.currentBranch) ? List.of(this.currentBranch.getAllowedInterruptions()) : List.of(this.lastBranch.getAllowedInterruptions());
    }

    private boolean hasInterruptions(Branch branch){
        return branch != null && branch.getAllowedInterruptions() != null;
    }

    public void close(){
        this.currentBranch = null;
        this.closed = true;
    }

    public void beforeRemoving(){
        this.executeOnCloseMethod();
    }

    private void executeOnCloseMethod() {
        if (this.controllerInstance == null) return;

        Optional<Method> onCloseMethod = Arrays.stream(this.controllerInstance.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(OnClose.class)).findFirst();

        if (onCloseMethod.isPresent()) {
            try {
                onCloseMethod.get().invoke(this.controllerInstance);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException("Unable to access OnClose method");
            } catch (InvocationTargetException e) {
                throw new TelegRiseRuntimeException("An exception occurred while closing the tree controller", e.getTargetException(), true);
            }
        }
    }

    private Branch getNextBranch(List<Branch> branches, ResourcePool resourcePool) {
        if (branches == null) return null;

        for (Branch branch : branches) {
            if(branch.getKeys() != null && resourcePool.getUpdate().hasMessage() && !MessageUtils.hasMedia(resourcePool.getUpdate().getMessage())
                    && Arrays.stream(branch.getKeys()).anyMatch(k -> k.equals(resourcePool.getUpdate().getMessage().getText())))
                return branch;
            else if (branch.getCallbackTriggers() != null && resourcePool.getUpdate().hasCallbackQuery()
                    && Arrays.stream(branch.getCallbackTriggers()).anyMatch(t -> t.equals(resourcePool.getUpdate().getCallbackQuery().getData())))
                return branch;
            else if (branch.getWhen() != null && branch.getWhen().generate(resourcePool))
                return branch;
        }

        return null;
    }

    public void open() {
        this.closed = false;
        this.naturallyClosed = false;
    }
}
