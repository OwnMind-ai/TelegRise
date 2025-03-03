package org.telegrise.telegrise.core;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.core.utils.ReflectionUtils;

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
                .filter(h -> !ReflectionUtils.annotation(h, Handler.class).afterTrees())
                .sorted(Comparator.comparingInt(h -> -ReflectionUtils.annotation(h, Handler.class).priority()))
                .filter(h -> h.canHandle(update))
                .findFirst();
    }

    public Optional<UpdateHandler> getApplicableAfterTreesHandler(Update update){
        return this.handlers.stream()
                .filter(h -> ReflectionUtils.annotation(h, Handler.class).afterTrees())
                .sorted(Comparator.comparingInt(h -> -ReflectionUtils.annotation(h, Handler.class).priority()))
                .filter(h -> h.canHandle(update))
                .findFirst();
    }

    public boolean applyHandler(Update update, UpdateHandler handler){
        try {
            handler.handle(update);
        } catch (TelegramApiException e) {
            handler.onException(e);
        }

        if (ReflectionUtils.hasAnnotation(handler, Handler.class)){
            Handler annotation = ReflectionUtils.annotation(handler, Handler.class);
            return annotation.absolute();
        }

        return false;
    }

    public void add(Class<? extends UpdateHandler> handlerClass){
        UpdateHandler instance = resourceInjector.createInstance(handlerClass);
        this.resourceInjector.injectResources(instance);
        this.handlers.add(instance);
    }
}
