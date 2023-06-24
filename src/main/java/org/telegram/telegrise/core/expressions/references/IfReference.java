package org.telegram.telegrise.core.expressions.references;

import org.jetbrains.annotations.NotNull;

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
    public Object invoke(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        Boolean condition = (Boolean) this.invokeReference(this.predicate, instance, args);

        if (condition){
            return this.invokeReference(this.doAction, instance, args);
        } else {
            return this.elseAction == null ? null : this.invokeReference(this.elseAction, instance, args);
        }
    }

    private Object invokeReference(ReferenceExpression reference, Object instance, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Map<Class<?>, Object> argsMap = Arrays.stream(args).collect(Collectors.toMap(Object::getClass, o -> o));

        Object[] requiredArgs = Arrays.stream(reference.parameterTypes()).map(argsMap::get).toArray();

        return reference.invoke(instance, requiredArgs);
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
