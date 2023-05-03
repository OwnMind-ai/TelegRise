package org.telegram.telegrise.core.expressions;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MethodReferenceParser {
    private static final Pattern STATIC_REFERENCE_PATTERN = Pattern.compile("[.\\w_]+#[\\w_]+");
    private static final Pattern INSTANCE_REFERENCE_PATTERN = Pattern.compile("(^|\\W)#[\\w_]+");

    public static MethodReference[] parse(String text, LocalNamespace namespace, Node node){
        return new MethodReferenceParser(namespace, node, text).parse();
    }

    public static <T> GeneratedValue<T> concat(MethodReference[] references, Class<T> returnType, Node node){
        assert references.length > 0;
        MethodReference toBeReturned = references[0];
        List<MethodReference> rest = references.length == 1 ? List.of() : List.of(references).subList(1, references.length);

        GeneratedValue<T> generatedValue = toBeReturned.toGeneratedValue(returnType, node);
        return (pool) -> {
            T result = generatedValue.generate(pool);
            rest.forEach(methodReference -> {
                try {
                    methodReference.invoke(pool);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

            return result;
        };
    }

    public static boolean isMethodReference(String expression){
        return isContainsInstanceMethodReference(expression) || STATIC_REFERENCE_PATTERN.matcher(expression).find();
    }

    public static boolean isContainsInstanceMethodReference(String expression){
        return INSTANCE_REFERENCE_PATTERN.matcher(expression).find();
    }

    private final Class<?> handlerClass;
    private final LocalNamespace namespace;
    private final List<Method> methods;
    private final Node node;
    private final String source;

    private MethodReferenceParser(LocalNamespace namespace, Node node, String source) {
        this.handlerClass = namespace.getHandlerClass();
        this.namespace = namespace;
        this.node = node;
        this.methods = this.handlerClass == null ? null : Arrays.stream(this.handlerClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Reference.class)).collect(Collectors.toList());
        this.source = source;
    }

    private MethodReference parseSingle(@NotNull String part){
        if (!part.trim().startsWith(Syntax.METHOD_REFERENCE_START))
            return this.parseStatic(part);

        String name = part.trim().substring(1);
        Method[] found = methods.stream().filter(m -> m.getName().equals(name)).toArray(Method[]::new);

        if (found.length == 0)
            throw new TranscriptionParsingException("Method '" + name + "' not found in class '" + this.handlerClass.getName() + "'", this.node);
        else if (found.length > 1)
            throw new TranscriptionParsingException("More than one method called '" + name + "' are decelerated in class '" + this.handlerClass.getName() + "'", this.node);
        else
            return new MethodReference(found[0], false);
    }

    private MethodReference parseStatic(String expression) {
        if (!STATIC_REFERENCE_PATTERN.matcher(expression).matches())
            throw new TranscriptionParsingException("Invalid syntax for method reference: '" + expression + "'", this.node);

        String[] parts = expression.split(Syntax.METHOD_REFERENCE_START);
        assert parts.length == 2;

        String className = parts[0];
        String methodName = parts[1];

        Class<?> actualClass = this.namespace.getApplicationNamespace().getClass(className);
        Method[] found = Arrays.stream(actualClass.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.isAnnotationPresent(Reference.class) && m.getName().equals(methodName)).toArray(Method[]::new);

        if (found.length == 0)
            throw new TranscriptionParsingException("Method '" + methodName + "' not found in class '" + actualClass.getName() + "'", this.node);
        else if (found.length > 1)
            throw new TranscriptionParsingException("More than one method called '" + methodName + "' are decelerated in class '" + actualClass.getName() + "'", this.node);
        else
            return new MethodReference(found[0], true);
    }

    private MethodReference[] parse(){
        String[][] raw = Arrays.stream(source.trim().split(Syntax.PARALLEL_SEPARATOR))
                .map(l -> l.trim().split(Syntax.CHAIN_SEPARATOR)).toArray(String[][]::new);
        List<MethodReference> references = new LinkedList<>();

        for (String[] chain : raw){
            if (chain.length == 0)
                throw new TranscriptionParsingException("Invalid syntax for method reference, unexpected ';'", this.node);

            MethodReference first = this.parseSingle(chain[0].trim());

            if (chain.length > 1){
                MethodReference last = first;

                for (int i = 1; i < chain.length; i++){
                    MethodReference next = this.parseSingle(chain[i].trim());

                    last.andThen(next);
                    last = next;
                }
            }

            references.add(first);
        }

        return references.toArray(new MethodReference[0]);
    }
}
