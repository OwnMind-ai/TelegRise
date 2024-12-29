package org.telegram.telegrise.core.expressions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.HiddenParameter;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.ReferenceGenerator;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.builtin.BuiltinReferences;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.expressions.references.*;
import org.telegram.telegrise.core.expressions.tokens.*;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.telegram.telegrise.generators.*;
import org.w3c.dom.Node;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.telegram.telegrise.core.expressions.references.MethodReference.compileParametersMapping;

@Slf4j
public class MethodReferenceCompiler {
    // Used to exclude reference duplication
    private final Map<AccessibleObject, ReferenceExpression> referenceMap = new HashMap<>();

    public ReferenceExpression compile(Token rootToken, LocalNamespace namespace, Class<?> returnType, Node node) {
        return switch (rootToken.getTokenType()) {
            case REFERENCE -> this.compileMethodReference((MethodReferenceToken) rootToken, namespace, node);
            case GENERATOR -> this.compileGenerator((ReferenceGeneratorToken) rootToken, namespace, node);
            case EXPRESSION -> this.compileExpression((ExpressionToken) rootToken, namespace, returnType, node);
            case IF_CONSTRUCTION -> this.compileIf((IfToken) rootToken, namespace, returnType, node);
            case VALUE -> this.compileValue((ValueToken) rootToken, returnType);
            default -> null;
        };
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

    @SuppressWarnings("unchecked")
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
                ReferenceExpression right = this.compile(token.getRight(), namespace, returnType, node);
                var leftType = right.parameterTypes().length != 1 ? Object.class : right.parameterTypes()[0];
                ReferenceExpression left = this.compile(token.getLeft(), namespace, leftType, node);
                boolean isLeftList = ClassUtils.isAssignable(left.returnType(), List.class);

                // TODO If left returns Object.class, then we skip type check. Needed for #memory,
                //  but there might be a better solution (like an annotation or wrapper idk)
                // Since 0.9.0
                if (!left.returnType().equals(Object.class) && (right.parameterTypes().length == 0 ||
                    (!isLeftList && !(right.parameterTypes()[0].isAssignableFrom(left.returnType()) ||
                        ClassUtils.primitiveToWrapper(right.parameterTypes()[0]).isAssignableFrom(ClassUtils.primitiveToWrapper(left.returnType())))))) {
                    throw new TranscriptionParsingException("Unable to apply '->' operator: left side returns different type '%s' than right side consumes '%s'"
                            .formatted(left.returnType().getName(), Arrays.stream(right.parameterTypes()).map(Class::getName).collect(Collectors.joining(", "))), node);
                }

                OperationReference<?, ?> reference = new OperationReference<>(right.returnType(), node);
                reference.setLeft(left);
                reference.setRight(right);
                reference.setParameters(left.parameterTypes());
                reference.setComposeRight(false);

                if (!isLeftList || right.parameterTypes().length == 1 && ClassUtils.isAssignable(right.parameterTypes()[0], List.class)) {
                    reference.setOperation((l, r) -> r.invoke(l.invoke()));
                } else if (right.parameterTypes().length == 1 && right.parameterTypes()[0].isArray()) {
                    reference.setOperation((l, r) -> {
                        List<?> list = (List<?>) l.invoke();
                        return r.invoke(new Object[]{ list.toArray() });
                    });
                } else {
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
            case Syntax.GREATER_OPERATOR, Syntax.LESS_OPERATOR, Syntax.LESS_OR_EQUALS_OPERATOR, Syntax.GREATER_OR_EQUALS_OPERATOR: {
                ReferenceExpression left = this.compile(token.getLeft(), namespace, Comparable.class, node);
                ReferenceExpression right = this.compile(token.getRight(), namespace, Comparable.class, node);

                String operator = token.getOperatorToken().getOperator();
                if (ClassUtils.isAssignable(left.returnType(), void.class))
                    throw new TranscriptionParsingException("Unable to apply '%s' operator: left side returns no value".formatted(operator), node);
                if (ClassUtils.isAssignable(right.returnType(), void.class))
                    throw new TranscriptionParsingException("Unable to apply '%s' operator: right side returns no value".formatted(operator), node);

                OperationReference<?, ?> reference = new OperationReference<>(Boolean.class, node);
                reference.setLeft(left);
                reference.setRight(right);

                switch (operator){
                    case Syntax.GREATER_OPERATOR -> reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        if(lv != null && rv != null && rv.getClass().equals(lv.getClass()))
                            return ((Comparable<Object>) lv).compareTo(rv) > 0;
                        else if (rv instanceof Number rn && lv instanceof Number ln)
                            return ln.doubleValue() > rn.doubleValue();
                        return false;
                    });
                    case Syntax.GREATER_OR_EQUALS_OPERATOR -> reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        if(lv != null && rv != null && rv.getClass().equals(lv.getClass()))
                            return ((Comparable<Object>) lv).compareTo(rv) >= 0;
                        else if (rv instanceof Number rn && lv instanceof Number ln)
                            return ln.doubleValue() >= rn.doubleValue();
                        return false;
                    });
                    case Syntax.LESS_OPERATOR -> reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        if(lv != null && rv != null && rv.getClass().equals(lv.getClass()))
                            return ((Comparable<Object>) lv).compareTo(rv) < 0;
                        else if (rv instanceof Number rn && lv instanceof Number ln)
                            return ln.doubleValue() < rn.doubleValue();
                        return false;
                    });
                    case Syntax.LESS_OR_EQUALS_OPERATOR -> reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        if(lv != null && rv != null && rv.getClass().equals(lv.getClass()))
                            return ((Comparable<Object>) lv).compareTo(rv) <= 0;
                        else if (rv instanceof Number rn && lv instanceof Number ln)
                            return ln.doubleValue() <= rn.doubleValue();
                        return false;
                    });
                    default -> throw new IllegalArgumentException(operator);
                }

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
                    reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        return Objects.equals(lv, rv) ||
                                (lv instanceof Number ln && rv instanceof Number rn && ln.doubleValue() == rn.doubleValue());
                    });
                else
                    reference.setOperation((l, r) -> {
                        Object lv = l.invoke(), rv = r.invoke();
                        return !Objects.equals(lv, rv) &&
                                !(lv instanceof Number ln && rv instanceof Number rn && ln.doubleValue() == rn.doubleValue());
                    });

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

    private ReferenceExpression compileGenerator(ReferenceGeneratorToken token, LocalNamespace namespace, Node node) {
        if (token.getClassName() == null && (BuiltinReferences.GENERATORS.contains(token.getMethod()) || token.getMethod().equals(Syntax.REGISTER))) {
            var dummy = new ReferenceGeneratorToken(BuiltinReferences.class.getName(), token.getMethod(), token.getParams());
            return compileGenerator(dummy, namespace, node);
        }

        //TODO static generators: can be generated right there once, for W performance
        Method method = (Method) getMethod(token, ReferenceGenerator.class, namespace, node);    // Must be Method and not Field
        ReferenceGenerator annotation = method.getAnnotation(ReferenceGenerator.class);

        if (!ClassUtils.isAssignable(method.getReturnType(), GeneratedReferenceBase.class))
            throw new TranscriptionParsingException("Reference generator named '%s' must return one of the following: %s".formatted(
                    token.getMethod(), Stream.of(GeneratedReference.class, GeneratedBiReference.class, GeneratedPolyReference.class, GeneratedVoidReference.class)
                            .map(Class::getSimpleName).collect(Collectors.joining(", "))), node);

        if (!ClassUtils.isAssignable(method.getReturnType(), GeneratedPolyReference.class) && annotation.parameters().length > 0) {
            throw new TranscriptionParsingException("Property @ReferenceGenerator::parameters conflicts with method's return type '" + method.getReturnType().getSimpleName() +
                    "': either remove parameters property or change return type to GeneratedPolyReference", node);
        }

        Type[] genericTypes = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
        List<Class<?>> generics = Arrays.stream(genericTypes)
                .peek(t -> {
                    if (!(t instanceof Class<?> || t instanceof ParameterizedType p && p.getRawType() instanceof Class<?>))
                        throw new TelegRiseRuntimeException("Unable to accept generator types: " + t.getTypeName());
                })
                .map(t -> t instanceof Class<?> c ? c : (Class<?>) ((ParameterizedType) t).getRawType())
                .toList();

        if (generics.isEmpty())
            throw new TelegRiseRuntimeException("Unable to accept generator named '%s'".formatted(method.getName()), node);

        boolean isVoid = ClassUtils.isAssignable(method.getReturnType(), GeneratedVoidReference.class);
        Class<?> returnType = isVoid ? void.class : generics.get(generics.size() - 1);
        Class<?>[] parameterTypes = isVoid ? new Class[]{generics.get(0)}
                    : annotation.parameters().length > 0 ? annotation.parameters()
                    : generics.subList(0, generics.size() - 1).toArray(new Class[0]);

        GeneratedValue<GeneratedReferenceBase> generator = compileParametrizedReference(method, token.getParams(), token.isStatic(), namespace, node)
                .toGeneratedValue(GeneratedReferenceBase.class, node);

        return new ReferenceExpression() {
            @Override
            public Object invoke(ResourcePool pool, Object instance, Object... args) {
                GeneratedReferenceBase generated = generator.generate(pool);

                //TODO add something better than 'invokeUnsafe'
                if (generated instanceof GeneratedReference<?,?> reference){
                    return reference.invokeUnsafe(args[0]);
                } else if (generated instanceof GeneratedBiReference<?,?,?> reference){
                    return reference.invokeUnsafe(args[0], args[1]);
                } else if (generated instanceof GeneratedPolyReference<?> reference){
                    return reference.run(args);
                } else if (generated instanceof GeneratedVoidReference<?> reference){
                    reference.invokeUnsafe(args[0]);
                    return null;
                } else
                    throw new TelegRiseRuntimeException("Invalid generated reference: " + generated.getClass().getName(), node);
            }

            @Override public @NotNull Class<?>[] parameterTypes() { return parameterTypes; }
            @Override public @NotNull Class<?> returnType() { return returnType; }
        };
    }

    private ReferenceExpression compileMethodReference(MethodReferenceToken token, LocalNamespace namespace, Node node) {
        if (token.getClassName() == null && (BuiltinReferences.METHODS.contains(token.getMethod()) || token.getMethod().equals(Syntax.REGISTER))) {
            var dummy = new MethodReferenceToken(BuiltinReferences.class.getName(), token.getMethod(), token.getParams());

            if (dummy.getMethod().equals("todo"))
                log.warn("Detected '#todo' reference at: {}", NodeElement.formatNode(node));

            return compileMethodReference(dummy, namespace, node);
        }

        AccessibleObject accessibleObject = getMethod(token, Reference.class, namespace, node);

        if (token.getParams() == null) {
            if (this.referenceMap.containsKey(accessibleObject)) {
                return this.referenceMap.get(accessibleObject);
            } else if (accessibleObject instanceof Method method){
                MethodReference methodReference = new MethodReference(method, token.isStatic());
                this.referenceMap.put(method, methodReference);
                return methodReference;
            } else if (accessibleObject instanceof Field field){
                FieldReference fieldReference = new FieldReference(field, token.isStatic());
                this.referenceMap.put(field, fieldReference);
                return fieldReference;
            } else
                throw new IllegalArgumentException(accessibleObject.toString());
        } else if (accessibleObject instanceof Method method)
            return this.compileParametrizedReference(method, token.getParams(), token.isStatic(), namespace, node);
        else
            throw new TranscriptionParsingException("Field reference '#%s' cannot take any parameters".formatted(token.getMethod()), node);
    }

    private static AccessibleObject getMethod(MethodContainer token, Class<? extends Annotation> annotation, LocalNamespace namespace, Node node) {
        if (!token.isStatic() && namespace.getHandlerClass() == null)
            throw new TranscriptionParsingException("Unable to compile method '" + token.getMethod() + "': no controller class is assigned", node);

        Class<?> parentClass = token.isStatic() ? namespace.getApplicationNamespace().getClass(token.getClassName()) : namespace.getHandlerClass();
        AccessibleObject method = getMethodFromClass(token, annotation, node, parentClass);
        if (method == null)      //TODO suggest possible methods ('did you mean..')
            throw new TranscriptionParsingException("Method '%s', that is annotated by @%s, was not found in class '%s' or its super classes"
                    .formatted(token.getMethod(), annotation.getSimpleName(), parentClass.getSimpleName()), node);

        method.setAccessible(true);
        return method;
    }

    private static AccessibleObject getMethodFromClass(MethodContainer token, Class<? extends Annotation> annotation, Node node, Class<?> parentClass) {
        AccessibleObject[] found = Stream.concat(Arrays.stream(parentClass.getDeclaredMethods()), Arrays.stream(parentClass.getDeclaredFields()))
                .filter(m -> m.isAnnotationPresent(annotation) && m.getName().equals(token.getMethod())).toArray(AccessibleObject[]::new);

        if (found.length == 0) {
            if (parentClass.getSuperclass() != null)
                return getMethodFromClass(token, annotation, node, parentClass.getSuperclass());
            return null;
        } else if (found.length > 1)
            throw new TranscriptionParsingException("More than one method called '" + token.getMethod() + "' are decelerated in class '" + parentClass.getName() + "'", node);

        return found[0];
    }

    private ReferenceExpression compileParametrizedReference(Method method, List<PrimitiveToken> parameters, boolean isStatic, LocalNamespace namespace, Node node) {
        if (parameters.stream().allMatch(ValueToken.class::isInstance) &&
                Arrays.stream(method.getParameters()).noneMatch(p -> p.getType().isArray() || p.isVarArgs()))
            return compileExplicitParametrizedReference(method, parameters, isStatic);

        boolean hasVarArg = method.getParameterCount() > 0 && method.getParameters()[method.getParameterCount() - 1].isVarArgs();
        String[] passedParameters = parameters.stream().map(PrimitiveToken::getStringValue).toArray(String[]::new);
        String[] actualParameters = new String[method.getParameterCount()];

        int i, a;
        for(i = 0, a = 0; i < actualParameters.length; i++){
            if(method.getParameters()[i].isAnnotationPresent(HiddenParameter.class))
                actualParameters[i] = namespace.getApplicationNamespace().getNameOfGlobal(method.getParameterTypes()[i]);
            else
                actualParameters[i] = passedParameters[a++];
        }

        if(hasVarArg && a < passedParameters.length){
            String[] temp = new String[method.getParameterCount() + passedParameters.length - a];
            System.arraycopy(actualParameters, 0, temp, 0, actualParameters.length);
            for(; i < temp.length; i++)
                temp[i] = passedParameters[a++];

            actualParameters = temp;
        } else
            assert a == passedParameters.length;

        String caller = isStatic ? method.getDeclaringClass().getName() : namespace.getApplicationNamespace().getControllerName();
        String expression = String.format("%s.%s(%s)", caller, method.getName(), String.join(", ", actualParameters));

        return getReferenceExpression(namespace, method.getReturnType(), node, expression);
    }

    private static @NotNull ReferenceExpression compileExplicitParametrizedReference(Method method, List<PrimitiveToken> parameters, boolean isStatic) {
        List<ValueToken> tokenList = parameters.stream().map(ValueToken.class::cast).toList();
        Object[] tokenParams = IntStream.range(0, tokenList.size())
                .mapToObj(i -> tokenList.get(i).getValue(method.getParameterTypes()[i]))
                .toArray();

        Class<?>[] parametersMapping = compileParametersMapping(method);

        return new ReferenceExpression() {
            @Override
            public Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
                return method.invoke(isStatic ? null : instance, MethodReference.compileArgs(tokenParams, pool, parametersMapping));
            }

            @Override
            public @NotNull Class<?>[] parameterTypes() {
                return new Class[0];
            }

            @Override
            public @NotNull Class<?> returnType() {
                return method.getReturnType();
            }
        };
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