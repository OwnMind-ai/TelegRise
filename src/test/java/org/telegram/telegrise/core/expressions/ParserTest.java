package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.expressions.tokens.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {
    private static final MethodReferenceToken FIRST_DUMMY = new MethodReferenceToken("first", null);
    private static final MethodReferenceToken SECOND_DUMMY = new MethodReferenceToken("second", null);
    private static final MethodReferenceToken THIRD_DUMMY = new MethodReferenceToken("third", null);

    private static final OperatorToken CHAIN_TOKEN = new OperatorToken(Syntax.CHAIN_SEPARATOR);
    private static final OperatorToken PARALLEL_TOKEN = new OperatorToken(Syntax.PARALLEL_SEPARATOR);
    private static final OperatorToken LIST_TOKEN = new OperatorToken(Syntax.LIST_SEPARATOR);
    private static final OperatorToken AND_TOKEN = new OperatorToken(Syntax.AND_OPERATOR);

    @Test
    void parse() throws ReferenceParsingException {
        Parser parser = new Parser(new Lexer(new CharsStream("#first")));
        assertEquals(FIRST_DUMMY, parser.parse());

        parser = new Parser(new Lexer(new CharsStream("#first -> #second")));
        assertEquals(new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, CHAIN_TOKEN), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("#first -> #second ; #third")));
        assertEquals(new ExpressionToken(
                new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, CHAIN_TOKEN),
                THIRD_DUMMY,
                PARALLEL_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("#first AND #second ; #third")));
        assertEquals(new ExpressionToken(
                new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, AND_TOKEN),
                THIRD_DUMMY,
                PARALLEL_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("#first AND #second -> #third")));
        assertEquals(new ExpressionToken(
                FIRST_DUMMY,
                new ExpressionToken(SECOND_DUMMY, THIRD_DUMMY, CHAIN_TOKEN),
                AND_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("(#first AND #second) -> #third")));
        assertEquals(new ExpressionToken(
                new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, AND_TOKEN),
                THIRD_DUMMY,
                CHAIN_TOKEN
        ), parser.parse());
        
        parser = new Parser(new Lexer(new CharsStream("(#first, #second) -> #third")));
        assertEquals(new ExpressionToken(
                new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, LIST_TOKEN),
                THIRD_DUMMY,
                CHAIN_TOKEN
        ), parser.parse());
        
        parser = new Parser(new Lexer(new CharsStream("#first, #second -> #third")));
        assertEquals(new ExpressionToken(
                FIRST_DUMMY,
                new ExpressionToken(SECOND_DUMMY, THIRD_DUMMY, CHAIN_TOKEN),
                LIST_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("((#first AND #second) -> #third) -> #first")));
        assertEquals(new ExpressionToken(
                new ExpressionToken(
                        new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, AND_TOKEN),
                        THIRD_DUMMY,
                        CHAIN_TOKEN
                ),
                FIRST_DUMMY,
                CHAIN_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("#first -> ::create(1)")));
        assertEquals(new ExpressionToken(
                FIRST_DUMMY,
                new ReferenceGeneratorToken(null, "create", List.of(new ValueToken(1L, Long.class))),
                CHAIN_TOKEN
        ), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("IF #first DO #second")));
        assertEquals(new IfToken(FIRST_DUMMY, SECOND_DUMMY, null), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("IF #first DO #second ELSE #third")));
        assertEquals(new IfToken(FIRST_DUMMY, SECOND_DUMMY, THIRD_DUMMY), parser.parse());

        parser = new Parser(new Lexer(new CharsStream("IF (#first AND #second) DO (#third ; #first)")));
        assertEquals(new IfToken(
                new ExpressionToken(FIRST_DUMMY, SECOND_DUMMY, AND_TOKEN),
                new ExpressionToken(THIRD_DUMMY, FIRST_DUMMY, PARALLEL_TOKEN),
                null), parser.parse());
    }
}