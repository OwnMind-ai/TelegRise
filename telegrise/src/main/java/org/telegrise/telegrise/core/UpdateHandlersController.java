package org.telegrise.telegrise.core;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UpdateHandlersController {
    private final List<UpdateHandler> handlers = new ArrayList<>();
    @Setter
    private ResourceInjector resourceInjector;

    public UpdateHandlersController(ResourceInjector injector) {
        this.resourceInjector = injector;
    }

    public Optional<UpdateHandler> getApplicableHandler(Update update){
        return this.handlers.stream()
                .filter(h -> !h.getClass().getAnnotation(Handler.class).afterTrees())
                .sorted(Comparator.comparingInt(h -> -h.getClass().getAnnotation(Handler.class).priority()))
                .filter(h -> h.canHandle(update))
                .findFirst();
    }

    public Optional<UpdateHandler> getApplicableAfterTreesHandler(Update update){
        return this.handlers.stream()
                .filter(h -> h.getClass().getAnnotation(Handler.class).afterTrees())
                .sorted(Comparator.comparingInt(h -> -h.getClass().getAnnotation(Handler.class).priority()))
                .filter(h -> h.canHandle(update))
                .findFirst();
    }

    public boolean applyHandler(Update update, UpdateHandler handler){
        try {
            handler.handle(update);
        } catch (TelegramApiException e) {
            handler.onException(e);
        }

        if (handler.getClass().isAnnotationPresent(Handler.class)){
            Handler annotation = handler.getClass().getAnnotation(Handler.class);

            return annotation.absolute();
        }

        return false;
    }

    public void add(Class<? extends UpdateHandler> handlerClass){
        UpdateHandler instance;
        try {
             instance = handlerClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new TelegRiseInternalException(e);
        } catch (InvocationTargetException e) {
            throw new TelegRiseInternalException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException  e) {
            throw new TelegRiseRuntimeException("Primary handler '" + handlerClass.getSimpleName() + "' must have a public constructor with no arguments");
        }

        this.resourceInjector.injectResources(instance);
        this.handlers.add(instance);
    }
}
