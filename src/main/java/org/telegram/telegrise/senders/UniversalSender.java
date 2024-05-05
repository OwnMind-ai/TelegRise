package org.telegram.telegrise.senders;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class UniversalSender {

    private static final Map<String, Method> methods = new HashMap<>();
    private static final String METHOD_NAME = "execute";

    static {
        Arrays.stream(BotSender.class.getMethods())
                .filter(method -> Objects.equals(method.getName(), METHOD_NAME)
                        && method.getParameterTypes().length == 1 && PartialBotApiMethod.class.isAssignableFrom(method.getParameterTypes()[0]))
                .forEach(m -> methods.put(m.getParameterTypes()[0].getName(), m));
    }

    private final BotSender sender;

    public UniversalSender(BotSender sender){
        this.sender = sender;
    }

    public Serializable execute(PartialBotApiMethod<? extends Serializable> method) throws TelegramApiException{
        if (method == null) return null;

        if (method instanceof BotApiMethod)
            return sender.execute((BotApiMethod<? extends Serializable>) method);

        try {
            return (Serializable) methods.get(method.getClass().getName()).invoke(sender, method);
        } catch (IllegalAccessException e) {
            throw new TelegRiseInternalException(e);
        } catch (InvocationTargetException e) {
            throw new TelegRiseInternalException(e.getTargetException());
        }
    }

    //TODO Move to package-private class
    public void execute(ActionElement action, ResourcePool pool) throws TelegramApiException {
        PartialBotApiMethod<?> method = action.generateMethod(pool);
        Object result = this.execute(method);

        if (result instanceof List<?> resultList) {
            if (!resultList.isEmpty() && resultList.get(0) instanceof Message)
                pool.getMemory().setLastSentMessage((Message) resultList.get(0));

        } else if (result instanceof Message) {
            pool.getMemory().setLastSentMessage((Message) result);
        }

        if (action.getReturnConsumer() != null && result != null)
            action.getConsumer(pool).consume(result);
    }
}
