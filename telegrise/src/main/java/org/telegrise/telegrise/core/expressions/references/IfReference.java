package org.telegrise.telegrise.core.expressions.references;

import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.ResourcePool;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IfReference implements ReferenceExpression{
    private final ReferenceExpression predicate, doAction, elseAction;

    public IfReference(ReferenceExpression predicate, ReferenceExpression doAction, ReferenceExpression elseAction) {
        this.predicate = predicate;
        this.doAction = doAction;
        this.elseAction = elseAction;
    }

    @Override
    public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        Boolean condition = (Boolean) this.invokeReference(this.predicate, pool, instance, args);

        if (condition){
            return this.invokeReference(this.doAction, pool, instance, args);
        } else {
            return this.elseAction == null ? null : this.invokeReference(this.elseAction, pool, instance, args);
        }
    }

    private Object invokeReference(ReferenceExpression reference, ResourcePool pool, Object instance, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Map<Class<?>, Object> argsMap = Arrays.stream(args).collect(Collectors.toMap(Object::getClass, o -> o));

        Object[] requiredArgs = Arrays.stream(reference.parameterTypes()).map(argsMap::get).toArray();

        return reference.invoke(pool, instance, requiredArgs);
    }

    @Override
    public @NotNull Class<?>[] parameterTypes() {
        Set<Class<?>> parameters = new HashSet<>(Arrays.asList(this.predicate.parameterTypes()));

        parameters.addAll(Arrays.asList(this.doAction.parameterTypes()));

        if (this.elseAction != null)
            parameters.addAll(Arrays.asList(this.elseAction.parameterTypes()));

        return parameters.toArray(Class[]::new);
    }

    @Override
    public @NotNull Class<?> returnType() {
        return this.doAction.returnType();
    }
}
