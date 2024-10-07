package org.telegram.telegrise.core.expressions.references;

import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    @Setter
    private boolean composeRight = true;
    @Setter
    private boolean composeLeft = true;
    private final Node node;  // For runtime errors

    public OperationReference(Class<?> returnType, Node node) {
        this.returnType = returnType;
        this.node = node;
    }

    @Override
    public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        ExpressionSupplier<L> leftSupplier = (overrideArgs) ->
                this.invokeSide(this.left, pool, instance, overrideArgs.length == 0 ? args : overrideArgs, composeLeft);
        ExpressionSupplier<R> rightSupplier = (overrideArgs) ->
                this.invokeSide(this.right, pool, instance, overrideArgs.length == 0 ? args : overrideArgs, composeRight);

        return operation.apply(leftSupplier, rightSupplier);
    }

    private <K> K invokeSide(ReferenceExpression reference, ResourcePool pool, Object instance, Object[] args,
                             boolean flexible) throws InvocationTargetException, IllegalAccessException {
        Object[] parameters = flexible ? composeParameters(reference, args) : args;

        // for cases like #getNull → (#first OPERATOR #second)
        if (!flexible && reference instanceof OperationReference<?, ?> r){
            r.setComposeRight(false);
            r.setComposeLeft(false);
        }

        //noinspection unchecked
        return (K) reference.invoke(pool, instance, parameters);
    }

    private Object @NotNull [] composeParameters(ReferenceExpression reference, Object[] args) {
        Map<Class<?>, List<Object>> components = Arrays.stream(args).collect(Collectors.groupingBy(Object::getClass, Collectors.toList()));

        if (!Arrays.stream(reference.parameterTypes()).map(p -> p.isPrimitive() ? ClassUtils.primitiveToWrapper(p) : p)
                .allMatch(p -> components.keySet().stream().anyMatch(c -> ClassUtils.isAssignable(c, p))))
            throw new TelegRiseRuntimeException("Illegal parameters set: {" + Arrays.stream(reference.parameterTypes())
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")) + "}");

        Object[] result = new Object[reference.parameterTypes().length];
        for(int i = 0; i < result.length; i++){
            Class<?> type = ClassUtils.primitiveToWrapper(reference.parameterTypes()[i]);
            try{
                List<Object> value = components.entrySet().stream()
                                .filter(e -> !e.getValue().isEmpty())
                                .filter(e -> ClassUtils.isAssignable(e.getKey(), type))
                                .map(Map.Entry::getValue).findFirst().orElseThrow();
                result[i] = value.remove(0);
            } catch(NullPointerException | IndexOutOfBoundsException | NoSuchElementException e){
                throw new TelegRiseRuntimeException("Unable to pass arguments of types %s to reference with parameters of types %s"
                    .formatted(Arrays.stream(args).map(Object::getClass).toList(), Arrays.deepToString(reference.parameterTypes())), node);
            }
        }

        return result;
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
