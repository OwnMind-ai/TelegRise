package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaExpressionPreprocessorTest {

    @Test
    void process() {
        JavaExpressionPreprocessor p = new JavaExpressionPreprocessor();

        assertEquals("\"Hello\"", p.process("\"Hello\""));
        assertEquals("\"Don't\"", p.process("\"Don't\""));
        assertEquals("\"Don''t\"", p.process("\"Don''t\""));

        assertEquals("\"Hello\"", p.process("''Hello''"));
        assertEquals("\"Don't\"", p.process("''Don't''"));
        assertEquals("\"Don\"t\"", p.process("''Don''t''"));

        assertEquals("\"a\"", p.process("''a''"));
        assertEquals("'a'", p.process("'a'"));

        assertEquals("\"Hello\" \"Don't\" \"Don\"t\"", p.process("''Hello'' ''Don't'' ''Don''t''"));
    }
}