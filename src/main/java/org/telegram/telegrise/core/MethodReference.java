package org.telegram.telegrise.core;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

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

    public Object invoke(Object instance, ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        Object result = this.invokeSingle(instance, pool);

        return this.next == null ? result : this.next.invokeWithNext(instance, result);
    }

    public Object invokeSingle(Object instance, ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        if (method.getParameterTypes().length == 0)
            return this.method.invoke(instance);
        else if (Arrays.equals(method.getParameterTypes(), new Class[]{Update.class})) {
            return this.method.invoke(instance, pool.getUpdate());
        } else {
            throw new UnsupportedOperationException();    //FIXME Not sure that there is any sense of ResourcePool, perhaps it would be useful in future
        }
    }

    private Object invokeWithNext(Object instance, Object parameter) throws InvocationTargetException, IllegalAccessException {
        Object result = this.method.getParameterTypes().length == 0 ?
                this.method.invoke(instance) : this.method.invoke(instance, parameter);

        return this.next == null ? result : this.next.invokeWithNext(instance, result);
    }

    public void andThen(MethodReference reference){
        assert reference.method.getParameterTypes().length == 1 &&
                reference.method.getParameterTypes()[0].isAssignableFrom(this.method.getReturnType());

        this.next = reference;
    }
}
