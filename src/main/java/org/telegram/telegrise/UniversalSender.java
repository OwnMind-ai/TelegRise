package org.telegram.telegrise;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//FIXME it's literally screaming for refactor
public class UniversalSender {
    private static final UniversalSender instance = new UniversalSender();

    static { instance.load(); }

    public static void execute(DefaultAbsSender sender, ActionElement action, ResourcePool pool) throws TelegramApiException {
        PartialBotApiMethod<?> method = action.generateMethod(pool);
        Object result = instance.execute(method, sender);

        if (result instanceof List<?>) {
            List<?> resultList = (List<?>) result;
            if (!resultList.isEmpty() && resultList.get(0) instanceof Message)
                pool.getMemory().setLastSentMessage((Message) resultList.get(0));

        } else if (result instanceof Message) {
            pool.getMemory().setLastSentMessage((Message) result);
        }

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



    public Object execute(PartialBotApiMethod<?> method, DefaultAbsSender sender) throws TelegramApiException {
        if (method == null) return null;

        if (method instanceof BotApiMethod)
            return sender.execute((BotApiMethod<?>) method);

        try {
            return this.methods.get(method.getClass().getName()).invoke(sender, method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }
}
