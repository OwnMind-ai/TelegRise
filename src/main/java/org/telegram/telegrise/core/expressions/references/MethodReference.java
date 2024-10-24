package org.telegram.telegrise.core.expressions.references;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.HiddenParameter;
import org.telegram.telegrise.caching.MethodReferenceCache;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

public class MethodReference implements ReferenceExpression{
    @Getter
    private transient Method method;
    /**
     * Length == method.getParametersCount(). If the element is not null,
     * then the parameter at the same index is annotated as @HiddenParameter,
     * and requires a global value of the type that the element is. Otherwise, it is a regular parameter.
     * See implementations of prepareParameters and compileArgs methods.
     */
    private transient Class<?>[] parametersMapping;
    private transient Class<?>[] requiredParameters;
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
        prepareParameters();
    }

    private void prepareParameters() {
        this.requiredParameters = Arrays.stream(method.getParameters())
                .filter(p -> !p.isAnnotationPresent(HiddenParameter.class))
                .map(Parameter::getType)
                .toArray(Class[]::new);

        this.parametersMapping = compileParametersMapping(method);
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
                result = method.invoke(null, compileArgs(args, pool, parametersMapping));
            else
                result = method.invoke(instance, compileArgs(args, pool, parametersMapping));

            if(cache != null)
                cache.write(result, pool);

            return result;
        }
    }

    public static Object[] compileArgs(Object[] args, ResourcePool pool, Class<?>[] mappings) {
        Map<Class<?>, Object> components = pool.getComponents();
        Object[] result = new Object[mappings.length];

        int i, a;
        for (i = 0, a = 0; i < mappings.length; i++) {
            Class<?> mapping = mappings[i];
            if (mapping != null)
                result[i] = ResourcePool.extractComponent(components, mapping);
            else
                result[i] = args[a++];
        }

        assert a == args.length;

        return result;
    }

    public static Class<?>[] compileParametersMapping(Method method) {
        return IntStream.range(0, method.getParameterCount())
                .mapToObj(i -> {
                    var param = method.getParameters()[i];
                    return param.isAnnotationPresent(HiddenParameter.class) ? param.getType() : null;
                })
                .toArray(Class[]::new);
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        return requiredParameters;
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
        prepareParameters();
    }

    @FunctionalInterface
    private interface MethodGetter extends Serializable {
        Method get(Class<?> clazz) throws NoSuchMethodException;
    }
}
