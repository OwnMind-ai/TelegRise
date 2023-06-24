package org.telegram.telegrise.core.expressions.references;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodReference implements ReferenceExpression{
    private transient Method method;
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
    public Object invoke(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        assert isStatic || instance != null : "Unable to invoke method reference: handler object in ResourcePool is null";

        if (isStatic)
            return method.invoke(null, args);
        else
            return method.invoke(instance, args);
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public @NotNull Class<?> returnType() {
        return method.getReturnType();
    }

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

    public static final ReferenceExpression NOT = new ReferenceExpression(){
        @Override
        public Object invoke(Object instance, Object... args) {
            return !((Boolean) args[0]);
        }

        @Override
        public @NotNull Class<?>[] parameterTypes() {
            return new Class[]{boolean.class};
        }

        @Override
        public @NotNull Class<?> returnType() {
            return boolean.class;
        }
    };
}
