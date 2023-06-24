package org.telegram.telegrise.core.expressions;

import org.apache.commons.lang3.ClassUtils;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Deprecated
public final class MethodReferenceOld implements Serializable {
    private transient Method method;
    private final @NotNull Class<?> declaringClass;
    private final @NotNull MethodGetter methodGetter;
    private final boolean isStatic;

    private MethodReferenceOld next;

    public MethodReferenceOld(Method method, boolean isStatic) {
        this.method = method;
        this.isStatic = isStatic;
        this.method.setAccessible(true);
        this.declaringClass = this.method.getDeclaringClass();

        String methodName = method.getName();
        Class<?>[] parameters = method.getParameterTypes();
        this.methodGetter = clazz -> clazz.getDeclaredMethod(methodName, parameters);
    }

    public Object invoke(ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        Object result = this.invokeSingle(pool);

        return this.next == null ? result : this.next.invokeWithNext(pool.getHandler(), result);
    }

    public Object invokeSingle(ResourcePool pool) throws InvocationTargetException, IllegalAccessException {
        Map<Class<?>, Object> components = pool.getComponents();

        if (!Arrays.stream(method.getParameterTypes()).allMatch(components::containsKey))
            throw new TelegRiseRuntimeException("Illegal parameters in method '" + this.method.getName() + "'");

        Object[] parameters = Arrays.stream(method.getParameterTypes())
                .map(components::get).toArray();

        return this.method.invoke(pool.getHandler(), parameters);
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
        if (!tClass.isAssignableFrom(actualReturnType) && !(actualReturnType.equals(void.class))
                && !(actualReturnType.equals(ClassUtils.wrapperToPrimitive(tClass))))
            throw new TranscriptionParsingException(String.format("Return type '%s' of method '%s' cannot be casted to type '%s'",
                    actualReturnType.getSimpleName(), last.getName(), tClass.getSimpleName()) , node);

        return pool -> {
            try {
                assert isStatic || pool.getHandler() != null : "Unable to invoke method reference: handler object in ResourcePool is null";
                Object result = invoke(pool);
                return tClass.equals(Void.class) ? null : tClass.cast(result);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }

    public void andThen(MethodReferenceOld reference){
        assert reference.method.getParameterTypes().length == 1 &&
                reference.method.getParameterTypes()[0].isAssignableFrom(this.method.getReturnType());

        this.next = reference;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException, NoSuchMethodException {
        stream.defaultReadObject();
        assert this.declaringClass != null && this.methodGetter != null : "Corrupted serialized object of class " + this.getClass().getSimpleName();

        this.method = this.methodGetter.get(declaringClass);
        this.method.setAccessible(true);
    }

    @FunctionalInterface
    private interface MethodGetter extends Serializable{
        Method get(Class<?> clazz) throws NoSuchMethodException;
    }
}
