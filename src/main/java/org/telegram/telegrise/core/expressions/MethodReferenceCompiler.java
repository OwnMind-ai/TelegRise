package org.telegram.telegrise.core.expressions;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.expressions.references.IfReference;
import org.telegram.telegrise.core.expressions.references.MethodReference;
import org.telegram.telegrise.core.expressions.references.OperationReference;
import org.telegram.telegrise.core.expressions.references.ReferenceExpression;
import org.telegram.telegrise.core.expressions.tokens.*;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodReferenceCompiler {

    public ReferenceExpression compile(Token rootToken, LocalNamespace namespace, Class<?> returnType, Node node) {
        if (rootToken.getTokenType() == TokenTypes.REFERENCE) {
            return this.compileMethodReference((MethodReferenceToken) rootToken, namespace, returnType, node);
        }

        if (rootToken.getTokenType() == TokenTypes.EXPRESSION){
            return this.compileExpression((ExpressionToken) rootToken, namespace, returnType, node);
        }

        if (rootToken.getTokenType() == TokenTypes.IF_CONSTRUCTION){
            return this.compileIf((IfToken) rootToken, namespace, returnType, node);
        }

        return null;
    }

    private ReferenceExpression compileIf(IfToken token, LocalNamespace namespace, Class<?> returnType, Node node) {
        ReferenceExpression predicate = this.compile(token.getPredicate(), namespace, Boolean.class, node);
        if (!ClassUtils.isAssignable(predicate.returnType(), Boolean.class))
            throw new TranscriptionParsingException("Unable to parse IF statement: if condition returns '" + predicate.returnType().getSimpleName() + "' type, not boolean type", node);

        ReferenceExpression doAction = this.compile(token.getDoAction(), namespace, returnType, node);
        ReferenceExpression elseAction = token.getElseAction() == null ? null : this.compile(token.getElseAction(), namespace, returnType, node);

        if (elseAction != null && !ClassUtils.isAssignable(elseAction.returnType(), doAction.returnType()))
            throw new TranscriptionParsingException("Unable to parse IF statement: DO and ELSE statements returns different types '"
                    + doAction.returnType().getSimpleName() + "' and '" + elseAction.returnType().getSimpleName() + "'", node);

        return new IfReference(predicate, doAction, elseAction);
    }

    private ReferenceExpression compileExpression(ExpressionToken token, LocalNamespace namespace, Class<?> returnType, Node node) {
        if(token.getOperatorToken().getOperator().equals(Syntax.AND_OPERATOR) || token.getOperatorToken().getOperator().equals(Syntax.OR_OPERATOR)){
            ReferenceExpression left = this.compile(token.getLeft(), namespace, Boolean.class, node);
            ReferenceExpression right = this.compile(token.getRight(), namespace,  Boolean.class, node);

            if (!Boolean.class.isAssignableFrom(left.returnType()) && !boolean.class.isAssignableFrom(left.returnType())){
                throw new TranscriptionParsingException(
                        "Unable to apply '" + token.getOperatorToken().getOperator() + "' operator: left side returns non-boolean value", node);
            }

            if (!Boolean.class.isAssignableFrom(right.returnType()) && !boolean.class.isAssignableFrom(right.returnType())){
                throw new TranscriptionParsingException(
                        "Unable to apply '" + token.getOperatorToken().getOperator() + "' operator: right side returns non-boolean value", node);
            }

            OperationReference<Boolean, Boolean> reference = new OperationReference<>(Boolean.class);
            reference.setLeft(left);
            reference.setRight(right);

            if (token.getOperatorToken().getOperator().equals(Syntax.AND_OPERATOR)){
                reference.setOperation((l, r) -> l.invoke() && r.invoke());
            } else {
                reference.setOperation((l, r) -> l.invoke() || r.invoke());
            }

            return reference;
        }

        if (token.getOperatorToken().getOperator().equals(Syntax.CHAIN_SEPARATOR)){
            ReferenceExpression left = this.compile(token.getLeft(), namespace, returnType, node);
            ReferenceExpression right = this.compile(token.getRight(), namespace, returnType, node);

            if (right.parameterTypes().length != 1 || !(right.parameterTypes()[0].isAssignableFrom(left.returnType())
                || ClassUtils.primitiveToWrapper(right.parameterTypes()[0]).isAssignableFrom(left.returnType())))
            {
                throw new TranscriptionParsingException("Unable to apply '->' operator: left side returns different type than right side consumes", node);
            }

            OperationReference<?, ?> reference = new OperationReference<>(right.returnType());
            reference.setLeft(left);
            reference.setRight(right);
            reference.setOperation((l, r) -> r.invoke(l.invoke()));
            reference.setParameters(left.parameterTypes());

            return reference;
        }

        if (token.getOperatorToken().getOperator().equals(Syntax.PARALLEL_SEPARATOR)){
            ReferenceExpression left = this.compile(token.getLeft(), namespace, returnType, node);
            ReferenceExpression right = this.compile(token.getRight(), namespace, returnType, node);

            OperationReference<?, ?> reference = new OperationReference<>(left.returnType());
            reference.setLeft(left);
            reference.setRight(right);
            reference.setOperation((l, r) -> {
                Object result = l.invoke();
                r.invoke();

                return result;
            });
            reference.setParameters(left.parameterTypes());

            return reference;
        }

        throw new IllegalArgumentException();
    }

    private ReferenceExpression compileMethodReference(MethodReferenceToken token, LocalNamespace namespace, Class<?> returnType, Node node) {
        if (token.getClassName() == null && token.getParams() == null && token.getMethod().equals(Syntax.NOT_REFERENCE))
            return MethodReference.NOT;

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

        if (token.getParams() == null) {
            return new MethodReference(method, token.isStatic());
        } else {
            return this.compileParametrizedReference(token, method, namespace, returnType, node);
        }
    }

    private ReferenceExpression compileParametrizedReference(MethodReferenceToken token, Method method, LocalNamespace namespace, Class<?> returnType, Node node) {
        String caller = token.isStatic() ? method.getDeclaringClass().getName() : namespace.getApplicationNamespace().getControllerName();

        String expression = String.format("%s.%s(%s)", caller, method.getName(), String.join(", ", token.getParams()));

        try {
            GeneratedValue<?> result = ExpressionFactory.getJavaExpressionCompiler().compile(expression, namespace, returnType, node);

            return new ReferenceExpression(){
                @Override
                public Object invoke(Object instance, Object... args) {
                    return result.generate((ResourcePool) args[0]);
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
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}