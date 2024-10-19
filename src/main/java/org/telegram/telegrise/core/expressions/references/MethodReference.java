package org.telegram.telegrise.core.expressions.references;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.caching.MethodReferenceCache;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodReference implements ReferenceExpression{
    @Getter
    private transient Method method;
    @Getter
    private final Class<?> declaringClass;
    private final MethodGetter methodGetter;
    private final boolean isStatic;

    public MethodReference(Method method, boolean isStatic) {
        this.method = method;
        this.isStatic = isStatic;

        method.setAccessible(true);
        this.declaringClass = method.getDeclaringClass();

        String methodName = method.getName();
        Class<?>[] parameters = method.getParameterTypes();
        this.methodGetter = clazz -> clazz.getDeclaredMethod(methodName, parameters);
    }

    @Override
    public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        if(!isStatic && instance == null)
            throw new TelegRiseRuntimeException("Unable to invoke method reference '%s': handler object in ResourcePool is null".formatted(method.getName()));

        MethodReferenceCache cache = pool.getMemory() != null ? pool.getMemory().getMethodReferenceCache(this) : null;
        if(cache != null && cache.isCacheApplicable(pool)){
            return cache.getCachedValue();
        } else {
            Object result;
            if (isStatic)
                result = method.invoke(null, args);
            else
                result = method.invoke(instance, args);

            if(cache != null)
                cache.write(result, pool);

            return result;
        }
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public @NotNull Class<?> returnType() {
        return method.getReturnType();
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException, NoSuchMethodException {
        stream.defaultReadObject();
        assert this.declaringClass != null && this.methodGetter != null : "Corrupted serialized object of class " + this.getClass().getSimpleName();

        this.method = this.methodGetter.get(declaringClass);
        this.method.setAccessible(true);
    }

    @FunctionalInterface
    private interface MethodGetter extends Serializable {
        Method get(Class<?> clazz) throws NoSuchMethodException;
    }
}
