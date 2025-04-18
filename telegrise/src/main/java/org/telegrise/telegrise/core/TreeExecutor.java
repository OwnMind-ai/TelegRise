package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.annotations.OnClose;
import org.telegrise.telegrise.core.elements.Branch;
import org.telegrise.telegrise.core.elements.DefaultBranch;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.actions.Edit;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.transition.ExecutionOptions;
import org.telegrise.telegrise.core.utils.ReflectionUtils;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.senders.UniversalSender;
import org.telegrise.telegrise.types.BotUser;
import org.telegrise.telegrise.utils.MessageUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public final class TreeExecutor {
    public static TreeExecutor create(Tree tree, ResourceInjector resourceInjector, BotSender sender, SessionMemoryImpl memory, BlockingQueue<Update> updatesQueue) {
        Object controller = null;
        if (tree.getController() != null)
            controller = new TreeControllerInitializer(tree.getController(), resourceInjector).initialize();

        return new TreeExecutor(memory, controller, tree, sender, updatesQueue, resourceInjector.get(BotUser.class));
    }

    public static void invokeBranch(GeneratedValue<Void> toInvoke, List<ActionElement> actions, ResourcePool pool, BotSender sender, ExecutionOptions options){
        if (options.execute() != null && !options.execute()) return;

        UniversalSender universalSender = new UniversalSender(sender);
        if (options.execute() != null) {
            if (toInvoke != null) toInvoke.generate(pool);
            if (actions == null) return;

            actions.forEach(action -> {
                try {
                    universalSender.execute(action, pool);
                } catch (TelegramApiException e) {
                    throw new TelegRiseInternalException(e);
                }
            });
        } else if (options.edit() != null && actions != null) {
            boolean isFirst = options.edit().equals(ExecutionOptions.EDIT_FIRST);
            for (ActionElement action : actions) {
                try {
                    //TODO creates action for every action, not singular one (for !isFirst). Can be optimized
                    Edit editAction = action.toEdit();
                    if (options.ignoreError() && editAction != null)
                        editAction.setOnError(p -> null);

                    if (isFirst && editAction != null) {
                        if (options.source() != null) editAction.setSource(options.source());
                        universalSender.execute(editAction, pool);
                        return;
                    } else if (!isFirst && options.edit().equals(action.getName())) {
                        if(editAction == null)
                            throw new TelegRiseRuntimeException("Unable to convert element named '%s' to edit action".formatted(options.edit()), action.getElementNode());

                        if (options.source() != null) editAction.setSource(options.source());
                        universalSender.execute(editAction, pool);
                        return;
                    }
                } catch (TelegramApiException e) {
                    if (options.ignoreError()) return;
                    throw new TelegRiseInternalException(e);
                }
            }
            throw new TelegRiseRuntimeException("Unable to find an element to edit after the transition", actions.getFirst().getElementNode());
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
    private final BotUser botUser;

    @Getter
    private Branch lastBranch;

    private TreeExecutor(SessionMemoryImpl memory, Object controllerInstance, Tree tree, BotSender sender, BlockingQueue<Update> updatesQueue, BotUser botUser) {
        this.memory = memory;
        this.controllerInstance = controllerInstance;
        this.tree = tree;
        this.sender = sender;
        this.updatesQueue = updatesQueue;
        this.botUser = botUser;
    }

    public boolean update(Update update){
        this.closed = false;
        List<Branch> nextBranches = currentBranch != null ? currentBranch.getBranches() : tree.getBranches();
        ResourcePool resourcePool = new ResourcePool(update, controllerInstance, this.sender, this.memory, botUser, this, updatesQueue);

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
            return true;
        }

        return false;
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

        Optional<Method> onCloseMethod = Arrays.stream(ReflectionUtils.getClass(this.controllerInstance).getMethods())
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
