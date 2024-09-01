package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.expressions.tokens.MethodReferenceToken;
import org.telegram.telegrise.core.expressions.tokens.RawToken;
import org.telegram.telegrise.core.expressions.tokens.ValueToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {
    @Test
    public void referenceTest() throws ReferenceParsingException {
        Lexer lexer = new Lexer(new CharsStream("#method"));
        assertEquals(new MethodReferenceToken("method", null), lexer.next());

        lexer = new Lexer(new CharsStream("Class#method"));
        assertEquals(new MethodReferenceToken("Class", "method", null), lexer.next());

        lexer = new Lexer(new CharsStream("org.Class#method"));
        assertEquals(new MethodReferenceToken("org.Class", "method", null), lexer.next());

        // Must read as a whole, not as "IF _ignore.Class#method"
        lexer = new Lexer(new CharsStream("if_ignore.Class#method"));
        assertEquals(new MethodReferenceToken("if_ignore.Class", "method", null), lexer.next());

        lexer = new Lexer(new CharsStream("#method(\"string 1\", 123)"));
        assertEquals(new MethodReferenceToken("method", List.of(new ValueToken("string 1", String.class), new ValueToken("123", Long.class))), lexer.next());

        lexer = new Lexer(new CharsStream("#method((1 + 2) / (3 - (3 + 1)), controller.getValue())"));
        assertEquals(new MethodReferenceToken("method", List.of(new RawToken("(1+2)/(3-(3+1))"), new RawToken("controller.getValue()"))), lexer.next());
    }
}