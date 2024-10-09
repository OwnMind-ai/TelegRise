package org.telegram.telegrise.core.expressions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.expressions.references.IfReference;
import org.telegram.telegrise.core.expressions.references.MethodReference;
import org.telegram.telegrise.core.expressions.references.OperationReference;
import org.telegram.telegrise.core.expressions.references.ReferenceExpression;
import org.telegram.telegrise.core.expressions.tokens.*;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class MethodReferenceCompiler {
    // Used to exclude reference duplication
    private final Map<Method, MethodReference> referenceMap = new HashMap<>();

    public ReferenceExpression compile(Token rootToken, LocalNamespace namespace, Class<?> returnType, Node node) {
        if (rootToken.getTokenType() == TokenTypes.REFERENCE) {
            return this.compileMethodReference((MethodReferenceToken) rootToken, namespace, node);
        }

        if (rootToken.getTokenType() == TokenTypes.EXPRESSION){
            return this.compileExpression((ExpressionToken) rootToken, namespace, returnType, node);
        }

        if (rootToken.getTokenType() == TokenTypes.IF_CONSTRUCTION){
            return this.compileIf((IfToken) rootToken, namespace, returnType, node);
        }

        if (rootToken.getTokenType() == TokenTypes.VALUE){
            return this.compileValue((ValueToken) rootToken, returnType);
        }

        return null;
    }

    private ReferenceExpression compileValue(ValueToken rootToken, Class<?> returnType) {
        Object value = rootToken.getValue(returnType);
        return new ReferenceExpression() {
            @Override
            public Object invoke(ResourcePool pool, Object instance, Object... args) { return value; }

            @Override
            public @NotNull Class<?>[] parameterTypes() {
                return new Class[0];
            }

            @Override
            public @NotNull Class<?> returnType() {
                return returnType;
            }
        };
    }

    private ReferenceExpression compileIf(IfToken token, LocalNamespace namespace, Class<?> returnType, Node node) {
        ReferenceExpression predicate = this.compile(token.getPredicate(), namespace, Boolean.class, node);
        if (!ClassUtils.isAssignable(predicate.returnType(), Boolean.class))
            throw new TranscriptionParsingException("Unable to parse IF statement: condition returns '" + predicate.returnType().getSimpleName() + "' type, not boolean type", node);

        ReferenceExpression doAction = this.compile(token.getDoAction(), namespace, returnType, node);
        ReferenceExpression elseAction = token.getElseAction() == null ? null : this.compile(token.getElseAction(), namespace, returnType, node);

        if (elseAction != null && !ClassUtils.isAssignable(elseAction.returnType(), doAction.returnType()) && 
                !ClassUtils.isAssignable(doAction.returnType(), elseAction.returnType()))
            throw new TranscriptionParsingException("Unable to parse IF statement: DO and ELSE statements returns different types '"
                    + doAction.returnType().getSimpleName() + "' and '" + elseAction.returnType().getSimpleName() + "'", node);

        return new IfReference(predicate, doAction, elseAction);
    }

    private ReferenceExpression compileExpression(ExpressionToken token, LocalNamespace namespace, Class<?> returnType, Node node) {
        switch (token.getOperatorToken().getOperator()) {
            case Syntax.AND_OPERATOR:
            case Syntax.OR_OPERATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, Boolean.class, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, Boolean.class, node);

                if (!Boolean.class.isAssignableFrom(left.returnType()) && !boolean.class.isAssignableFrom(left.returnType())) {
                    throw new TranscriptionParsingException(
                            "Unable to apply '" + token.getOperatorToken().getOperator() + "' operator: left side returns non-boolean value", node);
                }

                if (!Boolean.class.isAssignableFrom(right.returnType()) && !boolean.class.isAssignableFrom(right.returnType())) {
                    throw new TranscriptionParsingException(
                            "Unable to apply '" + token.getOperatorToken().getOperator() + "' operator: right side returns non-boolean value", node);
                }

                OperationReference<Boolean, Boolean> reference = new OperationReference<>(Boolean.class, node);
                reference.setLeft(left);
                reference.setRight(right);

                if (token.getOperatorToken().getOperator().equals(Syntax.AND_OPERATOR)) {
                    reference.setOperation((l, r) -> l.invoke() && r.invoke());
                } else {
                    reference.setOperation((l, r) -> l.invoke() || r.invoke());
                }

                return reference;
            }
            case Syntax.CHAIN_SEPARATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, returnType, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, returnType, node);
                boolean isLeftList = ClassUtils.isAssignable(left.returnType(), List.class);

                if (right.parameterTypes().length == 0 ||
                    (!isLeftList && !(right.parameterTypes()[0].isAssignableFrom(left.returnType()) || 
                        ClassUtils.primitiveToWrapper(right.parameterTypes()[0]).isAssignableFrom(left.returnType())))) {
                    throw new TranscriptionParsingException("Unable to apply '->' operator: left side returns different type than right side consumes", node);
                }

                OperationReference<?, ?> reference = new OperationReference<>(right.returnType(), node);
                reference.setLeft(left);
                reference.setRight(right);
                reference.setParameters(left.parameterTypes());

                if (!isLeftList || right.parameterTypes().length == 1 && ClassUtils.isAssignable(right.parameterTypes()[0], List.class)) {
                    reference.setComposeRight(false);
                    reference.setOperation((l, r) -> r.invoke(l.invoke()));
                } else {
                    reference.setComposeRight(true);
                    reference.setOperation((l, r) -> {
                        List<?> list = (List<?>) l.invoke();
                        return r.invoke(list.toArray());
                    });
                }

                return reference;
            }
            case Syntax.PARALLEL_SEPARATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, returnType, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, returnType, node);

                for (Class<?> leftParameter : left.parameterTypes()){
                    for (Class<?> rightParameter : right.parameterTypes()){
                        if (leftParameter != rightParameter &&
                                (ClassUtils.isAssignable(leftParameter, rightParameter) || ClassUtils.isAssignable(rightParameter, leftParameter))){
                            throw new TranscriptionParsingException("Unable to apply ';' operator: left side requires parameter of type '%s' which conflicts (assignable one to another) with parameter of type '%s' on the right".formatted(leftParameter.getSimpleName(), rightParameter.getSimpleName()), node);
                        }
                    }
                }

                OperationReference<?, ?> reference = new OperationReference<>(left.returnType(), node);
                reference.setLeft(left);
                reference.setRight(right);
                reference.setOperation((l, r) -> {
                    Object result = l.invoke();
                    r.invoke();

                    return result;
                });
                reference.setParameters(
                        Stream.concat(Arrays.stream(left.parameterTypes()), Arrays.stream(right.parameterTypes()))
                            .collect(Collectors.toSet()).toArray(Class[]::new)
                );

                return reference;
            }
            case Syntax.EQUALS_OPERATOR, Syntax.NOT_EQUALS_OPERATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, Object.class, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, Object.class, node);

                String operator = token.getOperatorToken().getOperator();
                if (ClassUtils.isAssignable(left.returnType(), void.class)) 
                    throw new TranscriptionParsingException("Unable to apply '%s' operator: left side returns no value".formatted(operator), node);
                if (ClassUtils.isAssignable(right.returnType(), void.class)) 
                    throw new TranscriptionParsingException("Unable to apply '%s' operator: right side returns no value".formatted(operator), node);

                OperationReference<?, ?> reference = new OperationReference<>(Boolean.class, node);
                reference.setLeft(left);
                reference.setRight(right);

                if(operator.equals(Syntax.EQUALS_OPERATOR))
                    reference.setOperation((l, r) -> Objects.equals(l.invoke(), r.invoke()));
                else
                    reference.setOperation((l, r) -> !Objects.equals(l.invoke(), r.invoke()));

                reference.setParameters(
                        Stream.concat(Arrays.stream(left.parameterTypes()), Arrays.stream(right.parameterTypes()))
                            .collect(Collectors.toSet()).toArray(Class[]::new)
                );

                return reference;
            }
            case Syntax.LIST_SEPARATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, Object.class, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, Object.class, node);

                OperationReference<?, ?> reference = new OperationReference<>(List.class, node);
                reference.setLeft(left);
                reference.setRight(right);
                reference.setParameters(
                        Stream.concat(Arrays.stream(left.parameterTypes()), Arrays.stream(right.parameterTypes()))
                            .collect(Collectors.toSet()).toArray(Class[]::new)
                );

                boolean lc = ClassUtils.isAssignable(left.returnType(), List.class);
                boolean rc = ClassUtils.isAssignable(right.returnType(), List.class);

                if (!lc && !rc) {
                    reference.setOperation((l, r) -> List.of(l.invoke(), r.invoke()));
                } else if(lc && !rc) {
                    reference.setOperation((l, r) -> {
                        Object first = l.invoke();
                        assert first instanceof List : "Left statement expected to return an instance of List, but got " + first.getClass().getName();

                        List<Object> copy = new ArrayList<>((List<?>) first);
                        copy.add(r.invoke());

                        return copy;
                    });
                } else if(!lc) {
                    reference.setOperation((l, r) -> {
                        Object second = r.invoke();
                        assert second instanceof List : "Right statement expected to return an instance of List, but got " + second.getClass().getName();

                        List<Object> copy = new ArrayList<>((List<?>) second);
                        copy.add(0, l.invoke());

                        return copy;
                    });
                } else {
                    reference.setOperation((l, r) -> {
                        Object first = l.invoke();
                        assert first instanceof List : "Left statement expected to return an instance of List, but got " + first.getClass().getName();

                        Object second = r.invoke();
                        assert second instanceof List : "Right statement expected to return an instance of List, but got " + second.getClass().getName();

                        List<Object> copy = new ArrayList<>((List<?>) first);
                        copy.addAll((List<?>) second);

                        return copy;
                    });
                }

                return reference;
            }
        }

        throw new IllegalArgumentException();
    }

    private ReferenceExpression compileMethodReference(MethodReferenceToken token, LocalNamespace namespace, Node node) {
        if (token.getClassName() == null && (BuiltinReferences.METHODS.contains(token.getMethod()) || token.getMethod().equals(Syntax.REGISTER))) {
            //TODO Replace with reference generator, deprecate after that (uncomment line below)
            if (token.getMethod().equals(Syntax.REGISTER) && token.getParams().size() == 1){
                var name = token.getParams().get(0).getStringValue();
//                log.warn("{}\nReference call '#register({})' is deprecated, replace with '::register({})'", NodeElement.formatNode(node), name, name);
                String expression = String.format("memory.putToRegistry(%s, java.util.Objects.requireNonNull(message))", name);
                return getReferenceExpression(namespace, Void.class, node, expression);
            }

            var dummy = new MethodReferenceToken(BuiltinReferences.class.getName(), token.getMethod(), token.getParams());
            return compileMethodReference(dummy, namespace, node);
        }

        if (!token.isStatic() && namespace.getHandlerClass() == null)
            throw new TranscriptionParsingException("Unable to parse method '" + token.getMethod() + "': no controller class is assigned", node);

        Class<?> parentClass = token.isStatic() ? namespace.getApplicationNamespace().getClass(token.getClassName()) : namespace.getHandlerClass();

        Method[] found = Arrays.stream(parentClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Reference.class) && m.getName().equals(token.getMethod())).toArray(Method[]::new);

        if (found.length == 0)
            throw new TranscriptionParsingException("Method '" + token.getMethod() + "' not found in class '" + parentClass.getName() + "'", node);
        else if (found.length > 1)
            throw new TranscriptionParsingException("More than one method called '" + token.getMethod() + "' are decelerated in class '" + parentClass.getName() + "'", node);

        Method method = found[0];

        if ((method.getModifiers() & Modifier.PUBLIC) == 0)
            throw new TranscriptionParsingException("Method '" + method.getName() + "' must be public", node);

        if (token.getParams() == null) {
            if (this.referenceMap.containsKey(method)) {
                return this.referenceMap.get(method);
            } else {
                //TODO temporary removed, decide later.
                // Maybe add a specific property for @Reference,
                // which indicates that method should take input only once, so the code below works correctly
//                CachingStrategy strategy = method.getAnnotation(Reference.class).caching();
//                if (strategy != CachingStrategy.NONE && strategy != CachingStrategy.UPDATE &&
//                        Arrays.asList(method.getParameterTypes()).contains(Update.class))
//                    throw new TranscriptionParsingException("Method with parameter of class Update cannot have caching strategy other than NONE or UPDATE. Current strategy: " + strategy, node);

                MethodReference methodReference = new MethodReference(method, token.isStatic());
                this.referenceMap.put(method, methodReference);
                return methodReference;
            }
        } else {
            return this.compileParametrizedReference(token, method, namespace, node);
        }
    }

    private ReferenceExpression compileParametrizedReference(MethodReferenceToken token, Method method, LocalNamespace namespace, Node node) {
        if (token.getParams().stream().allMatch(ValueToken.class::isInstance) && Arrays.stream(method.getParameterTypes()).noneMatch(Class::isArray)) {
            List<ValueToken> tokenList = token.getParams().stream().map(ValueToken.class::cast).toList();
            Object[] params = IntStream.range(0, tokenList.size())
                    .mapToObj(i -> tokenList.get(i).getValue(method.getParameterTypes()[i]))
                    .toArray();

            return new ReferenceExpression() {
                @Override
                public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
                    return method.invoke(token.isStatic() ? null : instance, params);
                }

                @Override
                public @NotNull Class<?>[] parameterTypes() {return new Class[0];}

                @Override
                public @NotNull Class<?> returnType() { return method.getReturnType(); }
            };
        }

        String caller = token.isStatic() ? method.getDeclaringClass().getName() : namespace.getApplicationNamespace().getControllerName();
        String expression = String.format("%s.%s(%s)", caller, method.getName(),
                token.getParams().stream().map(PrimitiveToken::getStringValue).collect(Collectors.joining(", ")));

        return getReferenceExpression(namespace, method.getReturnType(), node, expression);
    }

    private static @NotNull ReferenceExpression getReferenceExpression(LocalNamespace namespace, Class<?> returnType, Node node, String expression) {
        try {
            GeneratedValue<?> result = ExpressionFactory.getJavaExpressionCompiler().compile(expression, namespace, returnType, node);

            return new ReferenceExpression() {
                @Override
                public Object invoke(ResourcePool pool, Object instance, Object... args) {
                    return result.generate(pool);
                }

                @Override
                public @NotNull Class<?>[] parameterTypes() {
                    return new Class[]{ResourcePool.class};
                }

                @Override
                public @NotNull Class<?> returnType() {
                    return returnType;
                }

                @Override
                public <U> GeneratedValue<U> toGeneratedValue(Class<U> type, Node node) {
                    return pool -> type.cast(result.generate(pool));
                }
            };
        } catch (Exception e) { throw new TelegRiseInternalException(e); }
    }
}