package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.expressions.tokens.MethodReferenceToken;
import org.telegram.telegrise.core.expressions.tokens.StaticMethodReferenceToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {
    @Test
    public void referenceTest() throws ReferenceParsingException {
        Lexer lexer = new Lexer(new CharsStream("#method"));
        assertEquals(new MethodReferenceToken("method", null), lexer.next());

        lexer = new Lexer(new CharsStream("Class#method"));
        assertEquals(new StaticMethodReferenceToken("Class", "method", null), lexer.next());

        lexer = new Lexer(new CharsStream("#method(\"string 1\", 123)"));
        assertEquals(new MethodReferenceToken("method", List.of("\"string 1\"", "123")), lexer.next());

        lexer = new Lexer(new CharsStream("#method((1 + 2) / (3 - (3 + 1)), controller.getValue())"));
        assertEquals(new MethodReferenceToken("method", List.of("(1+2)/(3-(3+1))", "controller.getValue()")), lexer.next());
    }
}