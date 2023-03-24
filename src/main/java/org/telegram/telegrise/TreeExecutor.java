package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.DefaultBranch;
import org.telegram.telegrise.core.elements.Transition;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.ActionElement;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public final class TreeExecutor {
    public static TreeExecutor create(Tree tree, ResourceInjector resourceInjector, DefaultAbsSender sender) {
        try {
            Object handler = null;
            if (tree.getHandler() != null) {
                handler = tree.getHandler().getConstructor().newInstance();
                resourceInjector.injectResources(handler);
            }

            return new TreeExecutor(handler, tree, sender);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + tree.getHandler().getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with no arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    }

    public static void invokeBranch(GeneratedValue<Void> toInvoke, List<ActionElement> actions, ResourcePool pool, DefaultAbsSender sender){
        if (toInvoke != null) toInvoke.generate(pool);

        if (actions != null)
            actions.stream().map(action -> action.generateMethod(pool))
                    .forEach(m -> {
                        try {
                            UniversalSender.execute(sender, m, null);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
    }

    @Getter
    private final Object handlerInstance;
    @Getter
    private final Tree tree;
    @Getter
    private final DefaultAbsSender sender;
    @Getter @Setter
    private Branch currentBranch;
    @Getter
    private boolean closed;

    @Getter
    private Transition transition;

    public TreeExecutor(Object handlerInstance, Tree tree, DefaultAbsSender sender) {
        this.handlerInstance = handlerInstance;
        this.tree = tree;
        this.sender = sender;
    }

    public void update(Update update){
        this.closed = false;
        List<Branch> nextBranches = currentBranch != null ? currentBranch.getBranches() : tree.getBranches();
        ResourcePool resourcePool = new ResourcePool(update, handlerInstance);

        Branch previous = this.currentBranch;
        this.currentBranch = this.getNextBranch(nextBranches, resourcePool);

        if (this.currentBranch != null){
            this.invokeBranch(this.currentBranch.getToInvoke(), this.currentBranch.getActions(), resourcePool);

            if (this.currentBranch.getBranches() == null || this.currentBranch.getBranches().isEmpty()) {
                if (this.currentBranch.getTransition() != null)
                    this.transition = this.currentBranch.getTransition();

                this.close();
            }
        } else if(previous != null && previous.getDefaultBranch() != null) {
            DefaultBranch defaultBranch = previous.getDefaultBranch();
            this.invokeBranch(defaultBranch.getToInvoke(), defaultBranch.getActions(), resourcePool);

            this.currentBranch = previous;
        } else if(previous == null) {
            if (this.tree.getDefaultBranch() != null)
                this.invokeBranch(this.tree.getDefaultBranch().getToInvoke(), this.tree.getDefaultBranch().getActions(), resourcePool);
        } else {
            this.close();
        }
    }

    private void invokeBranch(GeneratedValue<Void> toInvoke, List<ActionElement> actions, ResourcePool pool){
        invokeBranch(toInvoke, actions, pool, this.sender);
    }

    public void close(){
        this.currentBranch = null;
        this.closed = true;
    }

    private Branch getNextBranch(List<Branch> branches, ResourcePool resourcePool) {
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

    public void clearTransition() {
        this.transition = null;
    }
}
