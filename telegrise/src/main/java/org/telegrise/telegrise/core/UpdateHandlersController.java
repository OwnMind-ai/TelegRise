package org.telegrise.telegrise.core;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.core.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateHandlersController {
    private final List<UpdateHandler> handlers = new ArrayList<>();
    private final List<UpdateHandler> absoluteHandlers = new ArrayList<>();

    @Setter
    private ResourceInjector resourceInjector;

    public UpdateHandlersController(ResourceInjector injector) {
        this.resourceInjector = injector;
    }

    public List<UpdateHandler> getApplicableHandlers(Update update){
        var handlers = getUpdateHandlerStream(this.handlers, update, false).collect(Collectors.toList());
        var absoluteHandler = getUpdateHandlerStream(this.absoluteHandlers, update, false).findFirst();

        absoluteHandler.ifPresent(handlers::add);

        return handlers;
    }

    public List<UpdateHandler> getApplicableAfterTreesHandler(Update update){
        var handlers = getUpdateHandlerStream(this.handlers, update, true).collect(Collectors.toList());
        var absoluteHandler = getUpdateHandlerStream(this.absoluteHandlers, update, true).findFirst();

        absoluteHandler.ifPresent(handlers::add);

        return handlers;
    }

    private @NotNull Stream<UpdateHandler> getUpdateHandlerStream(List<UpdateHandler> handlers, Update update, boolean needAfterTrees) {
        return handlers.stream()
                .filter(h -> needAfterTrees == ReflectionUtils.annotation(h, Handler.class).afterTrees())
                .sorted(Comparator.comparingInt(h -> -ReflectionUtils.annotation(h, Handler.class).priority()))
                .filter(h -> h.canHandle(update));
    }

    public boolean applyHandlers(Update update, List<UpdateHandler> handlers){
        for (UpdateHandler handler : handlers) {
            try {
                handler.handle(update);
            } catch (TelegramApiException e) {
                handler.onException(e);
            }

            Handler annotation = ReflectionUtils.annotation(handler, Handler.class);
            if (annotation.absolute()) {
                // We can 'safely' return here because absolute handlers are always at the end of the list
                return true;
            }
        }

        return false;
    }

    public void add(Class<? extends UpdateHandler> handlerClass){
        UpdateHandler instance = ResourceInjector.createInstance(handlerClass);
        this.resourceInjector.injectResources(instance);

        if (handlerClass.getAnnotation(Handler.class).absolute())
            this.absoluteHandlers.add(instance);
        else
            this.handlers.add(instance);
    }
}
