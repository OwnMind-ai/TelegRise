package org.telegram.telegrise;

import org.telegram.telegrise.core.elements.Tree;

import java.lang.reflect.InvocationTargetException;

public final class TreeExecutor {
    public static TreeExecutor create(Tree tree, ResourceInjector resourceInjector) {
        try {
            Object handler = tree.getHandler().getConstructor().newInstance();
            resourceInjector.injectResources(handler);

            return new TreeExecutor(handler, tree);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + tree.getHandler().getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with to arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    }

    private final Object handlerInstance;
    private final Tree tree;

    public TreeExecutor(Object handlerInstance, Tree tree) {
        this.handlerInstance = handlerInstance;
        this.tree = tree;
    }
}
