package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.expressions.references.ReferenceExpression;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

public class MethodReferenceCompilerTest {
    @Test
    void compile() throws ReferenceParsingException, ClassNotFoundException {
        MethodReferenceCompiler compiler = new MethodReferenceCompiler();
        LocalNamespace namespace = new LocalNamespace(MethodReferenceCompilerTest.class, new ApplicationNamespace(this.getClass().getClassLoader()));
        namespace.getApplicationNamespace().addClass(MethodReferenceCompilerTest.class.getName());
        ResourcePool pool = new ResourcePool(null, this, null, null);
        Node node = toNode("<tag/>");

        Parser parser = new Parser(new Lexer(new CharsStream("#first")));
        ReferenceExpression expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("MethodReferenceCompilerTest#getOne")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#second(false)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#first -> #second")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("MethodReferenceCompilerTest#getOne ; #first")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#first AND #third")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#first OR #third")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));
    }

    @Reference
    public boolean first(){
        return true;
    }

    @Reference
    public boolean third(){
        return false;
    }

    @Reference
    public boolean second(boolean b){
        return !b;
    }

    @Reference
    public static int getOne(){
        return 1;
    }
}