package org.telegram.telegrise.core;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.expressions.ExpressionParser;
import org.telegram.telegrise.core.expressions.MethodReferenceOld;
import org.telegram.telegrise.core.expressions.MethodReferenceParser;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionFactory {
    @Getter
    private static final ExpressionParser expressionParser = new ExpressionParser(ExpressionParser.getTempDirectory());

    public static @NotNull <T> GeneratedValue<T> createExpression(String text, Class<T> type, Node node, LocalNamespace namespace) {
        if(MethodReferenceParser.isMethodReference(text)){
            if (namespace.getHandlerClass() == null && MethodReferenceParser.isContainsInstanceMethodReference(text))
                throw new TranscriptionParsingException("Unable to parse method '" + text + "': no controller class is assigned", node);

            MethodReferenceOld[] references = MethodReferenceParser.parse(text, namespace, node);
            return MethodReferenceParser.concat(references, type, node);
        }

        if (type.equals(String.class))
            return parseFormattedString(text, type, node, namespace);

        if (text.trim().startsWith(Syntax.EXPRESSION_START) && text.trim().endsWith(Syntax.EXPRESSION_END)){
            String expression = text.trim().substring(Syntax.EXPRESSION_START.length(), text.trim().length() - Syntax.EXPRESSION_END.length());
            try {
                GeneratedValue<?> raw =  expressionParser.parse(expression, namespace, type, node);
                return p -> type.cast(raw.generate(p));
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        if (Number.class.isAssignableFrom(type)) {
            Number parsed;
            try {
                parsed = NumberFormat.getInstance().parse(text);
            } catch (ParseException e) { throw new RuntimeException(e); }

            if (type.equals(Integer.class))
                return GeneratedValue.ofValue(type.cast(parsed.intValue()));
            else if (type.equals(Long.class))
                return GeneratedValue.ofValue(type.cast(parsed.longValue()));
            else if (type.equals(Float.class))
                return GeneratedValue.ofValue(type.cast(parsed.floatValue()));
            else if (type.equals(Double.class))
                return GeneratedValue.ofValue(type.cast(parsed.doubleValue()));
            else if (type.equals(Short.class))
                return GeneratedValue.ofValue(type.cast(parsed.shortValue()));
            else if (type.equals(Byte.class))
                return GeneratedValue.ofValue(type.cast(parsed.byteValue()));
            else
                throw new TelegRiseRuntimeException("Unknown field number type");
        } else if (Boolean.class.isAssignableFrom(type)) {
            if (!text.equals("true") && !text.equals("false"))
                throw new TranscriptionParsingException("Cannot parse boolean value from '" + text + "'", node);
            return GeneratedValue.ofValue(type.cast(Boolean.parseBoolean(text)));
        } else {
            throw new TranscriptionParsingException("Unable to parse value '" + text + "'", node);
        }
    }

    private static <T> GeneratedValue<T> parseFormattedString(String text, Class<T> tClass, Node node, LocalNamespace namespace){
        return new StringExpressionParser(text, node, namespace).parse(tClass);
    }

    private static class StringExpressionParser{
        private static final char ESCAPE_CHAR = '\\';
        private final String source;
        private final Node node;
        private final LocalNamespace namespace;
        private int pointer = 0;

        private StringExpressionParser(String source, Node node, LocalNamespace namespace) {
            this.source = source;
            this.node = node;
            this.namespace = namespace;
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

            GeneratedValue<?> raw = expressionParser.parse(expression, namespace, String.class, node);
            return (pool) -> String.valueOf(raw.generate(pool));
        }
    }
}
