package org.telegram.telegrise.core.expressions;

import org.apache.commons.lang3.ClassUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class MethodReference {
    private final Method method;
    private MethodReference next;

    public MethodReference(Method method) {
        this.method = method;
        this.method.setAccessible(true);
    }

    public Object invoke(ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        Object result = this.invokeSingle(pool);

        return this.next == null ? result : this.next.invokeWithNext(pool.getHandler(), result);
    }

    public Object invokeSingle(ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        if (method.getParameterTypes().length == 0)
            return this.method.invoke(pool.getHandler());
        else if (Arrays.equals(method.getParameterTypes(), new Class[]{Update.class})) {
            return this.method.invoke(pool.getHandler(), pool.getUpdate());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Object invokeWithNext(Object instance, Object parameter) throws InvocationTargetException, IllegalAccessException {
        Object result = this.method.getParameterTypes().length == 0 ?
                this.method.invoke(instance) : this.method.invoke(instance, parameter);

        return this.next == null ? result : this.next.invokeWithNext(instance, result);
    }

    private Method getLast(){
        return this.next == null ? this.method : this.next.getLast();
    }

    public <T> GeneratedValue<T> toGeneratedValue(Class<T> tClass, Node node){
        Method last = this.getLast();
        Class<?> actualReturnType = last.getReturnType();
        if (!tClass.isAssignableFrom(actualReturnType) && !(ClassUtils.wrapperToPrimitive(tClass).equals(actualReturnType)))
            throw new TranscriptionParsingException(String.format("Return type '%s' of method '%s' cannot be casted to type '%s'",
                    actualReturnType.getSimpleName(), last.getName(), tClass.getSimpleName()) , node);

        return pool -> {
            try {
                assert pool.getHandler() != null : "Unable to invoke method reference: handler object in ResourcePool is null";
                Object result = invoke(pool);
                return tClass.equals(Void.class) ? null : tClass.cast(result);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }

    public void andThen(MethodReference reference){
        assert reference.method.getParameterTypes().length == 1 &&
                reference.method.getParameterTypes()[0].isAssignableFrom(this.method.getReturnType());

        this.next = reference;
    }
}
