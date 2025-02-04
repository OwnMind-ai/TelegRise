package org.telegrise.telegrise.senders;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.utils.ApiResponseWrapper;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.io.InvalidClassException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class is a wrapper for the {@link BotSender} class
 * that allows to execute any bot API method with a single method call {@link #execute(PartialBotApiMethod)}.
 * Additionally, it is used internally to execute ActionElements.
 *
 * @since 0.1
 */
public class UniversalSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniversalSender.class);

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

    /**
     * Executes any instance of {@link PartialBotApiMethod}
     * that can be executed in {@link BotSender} with a corresponding typed method.
     * Passing custom implementations of {@link PartialBotApiMethod} will cause {@code InvalidClassException}.
     *
     * @param method method to execute
     * @return corresponding return value
     */
    public Serializable execute(PartialBotApiMethod<? extends Serializable> method) throws TelegramApiException{
        if (method == null) return null;

        if (method instanceof BotApiMethod)
            return sender.execute((BotApiMethod<? extends Serializable>) method);

        try {
            var invokable = methods.get(method.getClass().getName());
            if (invokable == null)
                throw new InvalidClassException(method.getClass().getName(), "Unable to execute this implementation of PartialBotApiMethod");

            return (Serializable) invokable.invoke(sender, method);
        } catch (InvalidClassException | IllegalAccessException e) {
            LOGGER.error("An error occurred during invocation of an undefined api method", e);
            throw new TelegRiseInternalException(e);
        } catch (InvocationTargetException e) {
            LOGGER.error("An error occurred during invocation of an undefined api method", e.getTargetException());
            throw new TelegRiseInternalException(e.getTargetException());
        }
    }

    @ApiStatus.Internal
    public void execute(ActionElement action, ResourcePool pool) throws TelegramApiException {
        if (action.getWhen() != null && !action.getWhen().generate(pool)) return;

        PartialBotApiMethod<?> method;
        try {
            method = action.generateMethod(pool);
        } catch (Exception e){
            throw TelegRiseRuntimeException.unfold(e, action.getElementNode());
        }

        Object result;
        try {
            result = this.execute(method);
            LOGGER.debug("Action {} has been executed: {}\nAnd returned: {}", NodeElement.formatNode(action.getElementNode()), method, result);
        } catch (TelegramApiException e) {
            if (action.getOnError() != null){
                pool.addComponent(e);
                pool.addComponent(action.getElementNode());
                action.getOnError().generate(pool);
                return;
            } else {
                LOGGER.error("An error occurred while executing transcription action:\n\n{}\n", NodeElement.formatNode(action.getElementNode()), e);
                throw e;
            }
        }

        if (result instanceof List<?> resultList) {
            if (!resultList.isEmpty() && resultList.getFirst() instanceof Message)
                pool.getMemory().setLastSentMessage((Message) resultList.getFirst());

        } else if (result instanceof Message) {
            pool.getMemory().setLastSentMessage((Message) result);
        }

        if (action.getReturnConsumer() != null && result != null){
            pool.addComponent(result);
            pool.setApiResponseWrapper(new ApiResponseWrapper(result));
            action.getReturnConsumer().generate(pool);
        }
    }
}
