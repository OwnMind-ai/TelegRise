package org.telegrise.telegrise.core.expressions.references;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class FieldReference implements ReferenceExpression{
    private transient Field field;
    @Getter
    private final Class<?> declaringClass;
    private final FieldGetter fieldGetter;
    private final boolean isStatic;

    public FieldReference(Field field, boolean isStatic) {
        this.field = field;
        this.isStatic = isStatic;

        field.setAccessible(true);
        this.declaringClass = field.getDeclaringClass();

        String fieldName = field.getName();
        this.fieldGetter = clazz -> clazz.getDeclaredField(fieldName);
    }

    @Override
    public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        if(!isStatic && instance == null)
            throw new TelegRiseRuntimeException("Unable to invoke field reference '%s': handler object in ResourcePool is null".formatted(field.getName()));

        return field.get(isStatic ? null : instance);
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        return new Class[0];
    }

    @Override
    public @NotNull Class<?> returnType() {
        return field.getType();
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException, NoSuchFieldException {
        stream.defaultReadObject();
        assert this.declaringClass != null && this.fieldGetter != null : "Corrupted serialized object of class " + this.getClass().getSimpleName();

        this.field = this.fieldGetter.get(declaringClass);
        this.field.setAccessible(true);
    }

    @FunctionalInterface
    private interface FieldGetter extends Serializable {
        Field get(Class<?> clazz) throws NoSuchFieldException;
    }
}
