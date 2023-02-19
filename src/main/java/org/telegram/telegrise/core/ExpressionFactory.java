package org.telegram.telegrise.core;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.expressions.ExpressionParser;
import org.telegram.telegrise.core.expressions.MethodReference;
import org.telegram.telegrise.core.expressions.MethodReferenceParser;
import org.w3c.dom.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionFactory {
    private static final ExpressionParser expressionParser = new ExpressionParser(ExpressionParser.getTempDirectory());

    public static @NotNull GeneratedValue<Void> createExpression(String text, Node node, ResourcePool pool){
        return createExpression(text, Void.class, node, pool);
    }

    public static @NotNull <T> GeneratedValue<T> createExpression(String text, Class<T> type, Node node, ResourcePool pool) {
        if(MethodReferenceParser.isMethodReference(text)){
            MethodReference[] references = MethodReferenceParser.parse(text, type, node);
            return MethodReferenceParser.concat(references, type, node);
        }

        if (type.equals(String.class))
            return parseFormattedString(text, type, node, pool);

        throw new UnsupportedOperationException(); //FIXME
    }

    private static <T> GeneratedValue<T> parseFormattedString(String text, Class<T> tClass, Node node, ResourcePool pool){
        return new StringExpressionParser(text, node, pool).parse(tClass);
    }

    private static class StringExpressionParser{
        private static final char ESCAPE_CHAR = '\\';
        private final String source;
        private final Node node;
        private final ResourcePool pool;
        private int pointer = 0;

        private StringExpressionParser(String source, Node node, ResourcePool pool) {
            this.source = source;
            this.node = node;
            this.pool = pool;
        }

        public <T> GeneratedValue<T> parse(Class<T> tClass){
            List<GeneratedValue<String>> parts = new LinkedList<>();

            StringBuilder currentPart = new StringBuilder();
            while (pointer < source.length()){
                char ch = this.source.charAt(pointer);

                if (ch == ESCAPE_CHAR && pointer <= this.source.length() - 2) {
                    currentPart.append(source.charAt(pointer + 1));
                    pointer += 2;
                } else if (ch == Syntax.EXPRESSION_START.charAt(0) && this.source.substring(pointer).startsWith(Syntax.EXPRESSION_START)){
                    String current = currentPart.toString();
                    parts.add(pool -> current);
                    pointer += Syntax.EXPRESSION_START.length();

                    try {
                        parts.add(parseExpression());
                    } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                             InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    currentPart = new StringBuilder();
                } else {
                    currentPart.append(ch);
                    pointer++;
                }
            }

            String current = currentPart.toString();
            parts.add(pool -> current);

            return (pool) -> tClass.cast(parts.stream().map(i -> tClass.cast(i.generate(pool)).toString()).collect(Collectors.joining("")));
        }

        private GeneratedValue<String> parseExpression() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            String left = this.source.substring(this.pointer);
            String expression = left.substring(0, left.indexOf(Syntax.EXPRESSION_END));
            pointer += expression.length() + Syntax.EXPRESSION_END.length();

            GeneratedValue<?> raw = expressionParser.parse(expression, pool, String.class, node);
            return (pool) -> String.valueOf(raw.generate(pool));
        }
    }
}
