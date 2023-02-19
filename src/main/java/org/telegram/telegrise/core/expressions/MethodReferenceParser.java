package org.telegram.telegrise.core.expressions;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class MethodReferenceParser {
    public static MethodReference[] parse(String text, Class<?> clazz, Node node){
        return new MethodReferenceParser(clazz, node, text).parse();
    }

    private final Class<?> handlerClass;
    private final List<Method> methods;
    private final Node node;
    private final String source;

    private MethodReferenceParser(Class<?> handlerClass, Node node, String source) {
        this.handlerClass = handlerClass;
        this.node = node;
        this.methods = Arrays.stream(this.handlerClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Reference.class)).collect(Collectors.toList());
        this.source = source;
    }

    private MethodReference parseSingle(@NotNull String part){
        if (!part.trim().startsWith(Syntax.METHOD_REFERENCE_START))
            throw new TranscriptionParsingException("Invalid syntax for method reference", this.node);

        String name = part.trim().substring(1);
        Method[] found = methods.stream().filter(m -> m.getName().equals(name)).toArray(Method[]::new);

        if (found.length == 0)
            throw new TranscriptionParsingException("Method '" + name + "' not found in class '" + this.handlerClass.getName() + "'", this.node);
        else if (found.length > 1)
            throw new TranscriptionParsingException("More than one method called '" + name + "' are decelerated in class '" + this.handlerClass.getName() + "'", this.node);
        else
            return new MethodReference(found[0]);
    }

    private MethodReference[] parse(){
        String[][] raw = Arrays.stream(source.trim().split(Syntax.PARALLEL_SEPARATOR))
                .map(l -> l.trim().split(Syntax.CHAIN_SEPARATOR)).toArray(String[][]::new);
        List<MethodReference> references = new LinkedList<>();

        for (String[] chain : raw){
            if (chain.length == 0)
                throw new TranscriptionParsingException("Invalid syntax for method reference, unexpected ';'", this.node);

            MethodReference first = this.parseSingle(chain[0]);

            if (chain.length > 1){
                MethodReference last = first;

                for (int i = 1; i < chain.length; i++){
                    MethodReference next = this.parseSingle(chain[i]);

                    last.andThen(next);
                    last = next;
                }
            }

            references.add(first);
        }

        return references.toArray(new MethodReference[0]);
    }
}
