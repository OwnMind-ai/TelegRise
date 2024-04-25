package org.telegram.telegrise.core.expressions.references;

import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class OperationReference<L, R> implements ReferenceExpression{
    @Setter
    private ReferenceExpression left, right;
    @Setter
    private Operation<L, R> operation;
    @Setter
    private Class<?>[] parameters;
    private final Class<?> returnType;

    public OperationReference(Class<?> returnType) {
        this.returnType = returnType;
    }

    @Override
    public Object invoke(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        ExpressionSupplier<L> leftSupplier = (overrideArgs) ->
                this.invokeSide(this.left, instance, overrideArgs.length == 0 ? args : overrideArgs);
        ExpressionSupplier<R> rightSupplier = (overrideArgs) ->
                this.invokeSide(this.right, instance, overrideArgs.length == 0 ? args : overrideArgs);

        return operation.apply(leftSupplier, rightSupplier);
    }

    private <K> K invokeSide(ReferenceExpression reference, Object instance, Object[] args) throws InvocationTargetException, IllegalAccessException {
        //TODO fails if args contain null value
        Map<Class<?>, Object> components = Arrays.stream(args).collect(Collectors.toMap(Object::getClass, o -> o));

        if (!Arrays.stream(reference.parameterTypes()).map(p -> p.isPrimitive() ? ClassUtils.primitiveToWrapper(p) : p)
                .allMatch(p -> ResourcePool.extractComponent(components, p) != null))
            throw new TelegRiseRuntimeException("Illegal parameters set: {" + Arrays.stream(reference.parameterTypes())
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")) + "}");

        Object[] parameters = ClassUtils.isAssignable(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new), reference.parameterTypes()) ? args
                : Arrays.stream(reference.parameterTypes()).map(p -> ResourcePool.extractComponent(components, p)).toArray();

        return (K) reference.invoke(instance, parameters);
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        return parameters != null ? parameters :
                Stream.concat(
                    Arrays.stream(left.parameterTypes()), Arrays.stream(right.parameterTypes())
                ).collect(Collectors.toSet()).toArray(Class[]::new);
    }

    @Override
    public @NotNull Class<?> returnType() {
        return this.returnType;
    }

    @Override
    public <U> GeneratedValue<U> toGeneratedValue(Class<U> type, Node node) {
        return ReferenceExpression.super.toGeneratedValue(type, node);
    }

    @FunctionalInterface
    public interface Operation<L, R>{
        Object apply(ExpressionSupplier<L> left, ExpressionSupplier<R> right) throws InvocationTargetException, IllegalAccessException ;
    }

    @FunctionalInterface
    public interface ExpressionSupplier<T>{
        T invoke(Object... args) throws InvocationTargetException, IllegalAccessException ;
    }
}
