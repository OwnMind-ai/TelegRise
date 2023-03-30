package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.annotations.Handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrimaryHandlersController {
    private final List<PrimaryHandler> handlers = new ArrayList<>();
    private final ResourceInjector resourceInjector;

    public PrimaryHandlersController(ResourceInjector injector) {
        this.resourceInjector = injector;
    }

    public Optional<PrimaryHandler> getApplicableHandler(Update update){
        for (PrimaryHandler handler : this.handlers)
            if (handler.canHandle(update))
                return Optional.of(handler);

        return Optional.empty();
    }

    public boolean applyHandler(Update update, PrimaryHandler handler){
        handler.handle(update);

        if (handler.getClass().isAnnotationPresent(Handler.class)){
            Handler annotation = handler.getClass().getAnnotation(Handler.class);

            return annotation.absolute();
        }

        return false;
    }

    public void add(Class<? extends PrimaryHandler> handlerClass){
        PrimaryHandler instance;
        try {
             instance = handlerClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException  e) {
            throw new TelegRiseRuntimeException("Primary handler '" + handlerClass.getSimpleName() + "' must have a public constructor with no arguments");
        }

        this.resourceInjector.injectResources(instance);
        this.handlers.add(instance);
    }
}