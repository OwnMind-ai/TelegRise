package org.telegrise.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegrise.telegrise.core.expressions.tokens.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {
    @Test
    public void referenceTest() throws ReferenceParsingException {
        Lexer lexer = new Lexer(new CharsStream("123"));
        assertEquals(new ValueToken(123L, Long.class), lexer.next());

        lexer = new Lexer(new CharsStream("\"\""));
        assertEquals(new ValueToken("", String.class), lexer.next());

        lexer = new Lexer(new CharsStream("#method"));
        assertEquals(new MethodReferenceToken("method", null), lexer.next());

        lexer = new Lexer(new CharsStream("Class#method"));
        assertEquals(new MethodReferenceToken("Class", "method", null), lexer.next());

        lexer = new Lexer(new CharsStream("org.Class#method"));
        assertEquals(new MethodReferenceToken("org.Class", "method", null), lexer.next());

        // Must read as a whole, not as "IF _ignore.Class#method"
        lexer = new Lexer(new CharsStream("if_ignore.Class#method"));
        assertEquals(new MethodReferenceToken("if_ignore.Class", "method", null), lexer.next());

        lexer = new Lexer(new CharsStream("#method(\"\", 123)"));
        assertEquals(new MethodReferenceToken("method", List.of(new ValueToken("", String.class), new ValueToken(123L, Long.class))), lexer.next());

        lexer = new Lexer(new CharsStream("#method == null"));
        assertEquals(List.of(new MethodReferenceToken("method", null), new OperatorToken("=="), new ValueToken(null, Object.class)),
                List.of(lexer.next(), lexer.next(), lexer.next()));

        lexer = new Lexer(new CharsStream("#method((1 + 2) / (3 - (3 + 1)), controller.getValue())"));
        assertEquals(new MethodReferenceToken("method", List.of(new RawToken("(1+2)/(3-(3+1))"), new RawToken("controller.getValue()"))), lexer.next());

        lexer = new Lexer(new CharsStream("::method(1)"));
        assertEquals(new ReferenceGeneratorToken("method", List.of(new ValueToken(1L, Long.class))), lexer.next());

        lexer = new Lexer(new CharsStream("Class::method(1, \"a\", 1 + 2)"));
        assertEquals(new ReferenceGeneratorToken("Class", "method",
                List.of(new ValueToken(1L, Long.class), new ValueToken("a", String.class), new RawToken("1+2"))), lexer.next());
    }
}