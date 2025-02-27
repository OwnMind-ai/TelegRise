package org.telegrise.telegrise;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.senders.BotSender;

@ApiStatus.Experimental
public final class Expression<T> {
    private final GeneratedValue<T> value;

    @ApiStatus.Internal
    public Expression(GeneratedValue<T> value) {
        this.value = value;
    }

    public T get(@Nullable Update update){
        return get(update, null, null, null);
    }

    public T get(@Nullable Update update, @Nullable SessionMemory memory){
        return get(update, null, null, memory);
    }

    public T get(@Nullable Update update, @Nullable Object controller, @Nullable BotSender sender, @Nullable SessionMemory memory) {
        if (controller != null && !controller.getClass().isAnnotationPresent(TreeController.class))
            throw new IllegalArgumentException("Controller must be annotated as tree controller'");

        var actualMemory = memory == null ? null : (SessionMemoryImpl) memory;
        return value.generate(new ResourcePool(update, controller, sender, actualMemory, null));
    }
}
