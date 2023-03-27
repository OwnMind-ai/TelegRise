package org.telegram.telegrise;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UniversalSender {
    private static final UniversalSender instance = new UniversalSender();

    static { instance.load(); }

    public static <T extends Serializable> T execute(DefaultAbsSender sender, PartialBotApiMethod<T> method, Class<T> tClass) throws TelegramApiException {
        return instance.execute(method, sender, tClass);
    }

    public static void execute(DefaultAbsSender sender, ActionElement action, ResourcePool pool) throws TelegramApiException {
        PartialBotApiMethod<?> method = action.generateMethod(pool);
        Object result = instance.execute(method, sender, null);

        if (action.getReturnConsumer() != null && result != null)
            action.getConsumer(pool).consume(result);
    }


    private static final String METHOD_NAME = "execute";
    private final Map<String, Method> methods = new HashMap<>();

    private UniversalSender(){}

    public void load(){
        Arrays.stream(DefaultAbsSender.class.getMethods())
                .filter(method -> method.getName().equals(METHOD_NAME)
                        && method.getParameterTypes().length == 1 && PartialBotApiMethod.class.isAssignableFrom(method.getParameterTypes()[0]))
                .forEach(m -> this.methods.put(m.getParameterTypes()[0].getName(), m));
    }



    public <T extends Serializable> T execute(PartialBotApiMethod<T> method, DefaultAbsSender sender, Class<T> tClass) throws TelegramApiException {
        if (method == null) return null;

        if (method instanceof BotApiMethod)
            return sender.execute((BotApiMethod<T>) method);

        try {
            Object result = this.methods.get(method.getClass().getName()).invoke(sender, method);

            return tClass != null && tClass.isInstance(result) ? tClass.cast(result) : null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
