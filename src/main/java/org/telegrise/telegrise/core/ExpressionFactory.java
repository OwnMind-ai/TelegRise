package org.telegrise.telegrise.core;

import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.expressions.*;
import org.telegrise.telegrise.core.expressions.tokens.Token;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
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
    private static final JavaExpressionCompiler javaExpressionCompiler = new JavaExpressionCompiler(JavaExpressionCompiler.getTempDirectory());
    private static final MethodReferenceCompiler methodReferenceCompiler = new MethodReferenceCompiler();

    public static @NotNull <T> GeneratedValue<T> createExpression(String text, Class<T> type, Node node, LocalNamespace namespace) {
        Parser parser = new Parser(new Lexer(new CharsStream(text)));

        try {
            Token rootToken = parser.parse();
            return methodReferenceCompiler.compile(rootToken, namespace, type, node).toGeneratedValue(type, node);
        } catch (ReferenceParsingException e) {
            if (type.equals(String.class))
                return parseFormattedString(text, type, node, namespace);

            if (text.trim().startsWith(Syntax.EXPRESSION_START) && text.trim().endsWith(Syntax.EXPRESSION_END)) {
                String expression = text.trim().substring(Syntax.EXPRESSION_START.length(), text.trim().length() - Syntax.EXPRESSION_END.length());
                try {
                    GeneratedValue<?> raw = javaExpressionCompiler.compile(expression, namespace, type, node);
                    return p -> type.cast(raw.generate(p));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }

            if (e.getErrorCode() == ErrorCodes.UNDEFINED_TOKEN)
                return parsePrimitive(text, type, node);
            else
                throw new TranscriptionParsingException(e.getMessage(), node);
        }
    }

    @NotNull
    private static <T> GeneratedValue<T> parsePrimitive(String text, Class<T> type, Node node) {
        if (ClassUtils.isAssignable(type, Number.class)) {
            Number parsed;
            try {
                parsed = NumberFormat.getInstance().parse(text);
            } catch (ParseException e) { throw new TelegRiseInternalException(e); }

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
        } else if (ClassUtils.isAssignable(type, Boolean.class)) {
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
                    parts.add(GeneratedValue.ofValue(current));
                    pointer += Syntax.EXPRESSION_START.length();

                    try {
                        parts.add(parseExpression());
                    } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                             InstantiationException | IllegalAccessException e) {
                        throw new TelegRiseInternalException(e);
                    }

                    currentPart = new StringBuilder();
                } else {
                    currentPart.append(ch);
                    pointer++;
                }
            }

            String current = currentPart.toString();
            parts.add(GeneratedValue.ofValue(current));

            return normalized(parts, tClass);
        }

        private <T> GeneratedValue<T> normalized(List<GeneratedValue<String>> parts, Class<T> tClass) {
            if (parts.stream().allMatch(GeneratedValue.StaticValue.class::isInstance)){
                String value = parts.stream().map(i -> tClass.cast(i.generate(null)).toString()).collect(Collectors.joining(""));

                return GeneratedValue.ofValue(tClass.cast(value));
            }

            return (pool) -> tClass.cast(parts.stream().map(i -> tClass.cast(i.generate(pool)).toString()).collect(Collectors.joining("")));
        }

        private GeneratedValue<String> parseExpression() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            String left = this.source.substring(this.pointer);
            String expression = left.substring(0, left.indexOf(Syntax.EXPRESSION_END));
            pointer += expression.length() + Syntax.EXPRESSION_END.length();

            Parser parser = new Parser(new Lexer(new CharsStream(expression)));

            try {
                Token rootToken = parser.parse();
                GeneratedValue<?> raw = methodReferenceCompiler.compile(rootToken, namespace, Object.class, node).toGeneratedValue(Object.class, node);
                return (pool) -> String.valueOf(raw.generate(pool));
            } catch (ReferenceParsingException e) {
                if (e.getErrorCode() == ErrorCodes.UNDEFINED_TOKEN){
                    GeneratedValue<?> raw = javaExpressionCompiler.compile(expression, namespace, String.class, node);
                    return (pool) -> String.valueOf(raw.generate(pool));
                } else {
                    throw new TranscriptionParsingException(e.getMessage(), node);
                }
            }
        }
    }
}
