package org.telegram.telegrise;

import lombok.Getter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.Tree;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class TreeExecutor {
    public static TreeExecutor create(Tree tree, ResourceInjector resourceInjector, DefaultAbsSender sender) {
        try {
            Object handler = tree.getHandler().getConstructor().newInstance();
            resourceInjector.injectResources(handler);

            return new TreeExecutor(handler, tree, sender);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + tree.getHandler().getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with to arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    }

    @Getter
    private final Object handlerInstance;
    @Getter
    private final Tree tree;
    private final DefaultAbsSender sender;
    @Getter
    private Branch currentBranch;
    @Getter
    private boolean closed;

    public TreeExecutor(Object handlerInstance, Tree tree, DefaultAbsSender sender) {
        this.handlerInstance = handlerInstance;
        this.tree = tree;
        this.sender = sender;
    }

    public void update(Update update){
        List<Branch> nextBranches = currentBranch != null ? currentBranch.getBranches() : tree.getBranches();
        ResourcePool resourcePool = new ResourcePool(update, handlerInstance);

        this.currentBranch = this.getNextBranch(nextBranches, resourcePool);

        if (this.currentBranch != null){
            this.invokeBranch(this.currentBranch, resourcePool);

            if (this.currentBranch.getBranches() == null || this.currentBranch.getBranches().isEmpty())
                this.close();
        } //TODO Otherwise invoke default branch
    }

    private void invokeBranch(Branch branch, ResourcePool pool){
        if (branch.getToInvoke() != null)
            branch.getToInvoke().generate(pool);

        if (branch.getActions() != null)
            branch.getActions().stream().map(action -> action.generateMethod(pool))
                    .forEach(m -> {
                        try {
                            this.sender.execute(m);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
    }

    private void close(){
        this.closed = true;
    }

    private Branch getNextBranch(List<Branch> branches, ResourcePool resourcePool) {
        for (Branch branch : branches)
            if (branch.getWhen().generate(resourcePool))
                return branch;

        return null;
    }
}
