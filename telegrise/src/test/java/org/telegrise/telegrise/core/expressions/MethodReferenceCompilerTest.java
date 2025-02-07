package org.telegrise.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.annotations.HiddenParameter;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.ReferenceGenerator;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.expressions.references.ReferenceExpression;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.generators.GeneratedPolyReference;
import org.telegrise.telegrise.generators.GeneratedReference;
import org.telegrise.telegrise.generators.GeneratedVoidReference;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.telegrise.telegrise.core.parser.XMLElementsParserTest.toNode;

public class MethodReferenceCompilerTest {
    @Test
    void compile() throws ReferenceParsingException, ClassNotFoundException {
        MethodReferenceCompiler compiler = new MethodReferenceCompiler();
        LocalNamespace namespace = new LocalNamespace(MethodReferenceCompilerTest.class, new ApplicationNamespace(this.getClass().getClassLoader(),""));
        namespace.getApplicationNamespace().addClass(MethodReferenceCompilerTest.class.getName());
        ResourcePool pool = new ResourcePool(null, this, null, new SessionMemoryImpl(0, SessionIdentifier.ofUserOnly(0L), ""));
        Node node = toNode("<tag/>");

        Parser parser = new Parser(new Lexer(new CharsStream("\"value\"")));
        ReferenceExpression expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("value", expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("123")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(123, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#first")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("MethodReferenceCompilerTest#getOne")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#second(false)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#env(\"JAVA_HOME\")")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        ReferenceExpression finalExpression = expression;
        assertDoesNotThrow(() -> finalExpression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#setNum(123)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(123, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#second(!true)")));
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
        expression = compiler.compile(parser.parse(), namespace, Object.class, node);
        assertEquals(true, expression.toGeneratedValue(Object.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#first OR #third) -> #not")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#first OR #third) -> (#second ; #second)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#first OR #third) -> (#second -> #second)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("true -> (#second -> #second)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF #first DO MethodReferenceCompilerTest#getOne")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF #first DO 123 ELSE 456")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(123, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF #first -> #not DO 123 ELSE (#first OR #third) -> (#second -> #second")));
        expression = compiler.compile(parser.parse(), namespace, Object.class, node);
        assertEquals(true, expression.toGeneratedValue(Object.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF #first -> (#not -> #not) -> #not DO 123 ELSE MethodReferenceCompilerTest#getOne")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF false DO #getNull ELSE \"B\"")));
        expression = compiler.compile(parser.parse(), namespace, Object.class, node);
        assertEquals("B", expression.toGeneratedValue(Object.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF (#first -> #not) DO MethodReferenceCompilerTest#getOne ELSE MethodReferenceCompilerTest#getTwo")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(2, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertNull(expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> #consume")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("nullA", expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("\"a\" -> #isNull")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> (#consume -> #consume)")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("nullAA", expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF (#getNull -> #isNull) DO MethodReferenceCompilerTest#getOne ELSE MethodReferenceCompilerTest#getTwo")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF (#getNull -> #isNull -> #not) DO #getNull ELSE #getNull -> #consume")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("nullA", expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> (#isNull AND #isNull)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> (#isNull ; #isNull)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#getOne, #getTwo -> #setNum)")));
        expression = compiler.compile(parser.parse(), namespace, List.class, node);
        assertEquals(List.of(1, 2), expression.toGeneratedValue(List.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#getOne, #getTwo, (#getTwo, #getOne), \"ABC\")")));
        expression = compiler.compile(parser.parse(), namespace, List.class, node);
        assertEquals(List.of(1, 2, 2, 1, "ABC"), expression.toGeneratedValue(List.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#first, #third) -> #xor")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("\"ABC\" -> (#consume, #isNull) -> #concat")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("ABCAfalse", expression.toGeneratedValue(String.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> #consume == \"nullA\"")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> #consume != \"nullA\"")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(false, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getNull -> (#consume == \"nullA\" AND #isNull)")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#getOne, #getLongTwelve) -> #sum")));
        expression = compiler.compile(parser.parse(), namespace, Long.class, node);
        assertEquals(13L, expression.toGeneratedValue(Long.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(#getOne, #getLongTwelve) -> #sum > 0")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#getTwo -> ::pow(6)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(64, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(\"A\", 1, \"C\") -> ::join(\", \", 2)")));
        expression = compiler.compile(parser.parse(), namespace, String.class, node);
        assertEquals("A, 1", expression.toGeneratedValue(String.class, node).generate(pool));

        var original = System.out;
        var mock = new ByteArrayOutputStream();
        System.setOut(new PrintStream(mock));

        parser = new Parser(new Lexer(new CharsStream("(\"A\", \"B\") -> ::printf(\"%s: %s\")")));
        expression = compiler.compile(parser.parse(), namespace, Void.class, node);
        expression.toGeneratedValue(Void.class, node).generate(pool);

        System.setOut(original);
        assertEquals("A: B", mock.toString());

        parser = new Parser(new Lexer(new CharsStream("\"foo\" -> #hidden")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(\"foo\", \"boo\") -> #hidden2")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#hidden(\"foo\")")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#hidden(\"fo\" + \"o\")")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("(\"A\", \"B\") -> ::mprintf(\"%s: %s\", \"mprintfResult\")")));
        expression = compiler.compile(parser.parse(), namespace, Void.class, node);
        expression.toGeneratedValue(Void.class, node).generate(pool);
        assertEquals("A: B", pool.getMemory().get("mprintfResult"));

        parser = new Parser(new Lexer(new CharsStream("#add(1)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(1, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#add(1, 4, 6)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(11, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#add(1, 4, 6) ; #add(1)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(11, expression.toGeneratedValue(Integer.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#setNum(4) >= 4")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("#setNum(4) != 5")));
        expression = compiler.compile(parser.parse(), namespace, Boolean.class, node);
        assertEquals(true, expression.toGeneratedValue(Boolean.class, node).generate(pool));

        parser = new Parser(new Lexer(new CharsStream("IF false DO #setNum(1) ELSE IF true DO #setNum(2) ELSE #setNum(3)")));
        expression = compiler.compile(parser.parse(), namespace, Integer.class, node);
        assertEquals(2, expression.toGeneratedValue(Integer.class, node).generate(pool));
    }

    @Reference
    public int add(@HiddenParameter SessionMemory memory, int... values){
        return Arrays.stream(values).sum();
    }

    @ReferenceGenerator
    public GeneratedVoidReference<List<String>> mprintf(String format, String name, @HiddenParameter SessionMemory memory){
        return l -> memory.put(name, format.formatted(l.toArray()));
    }

    @Reference  // "if hidden is so good, why isn't there hidden 2?" There it is
    public boolean hidden2(String ignore, String ignore2, @HiddenParameter SessionMemory memory){
        return memory != null;
    }

    @Reference
    public boolean hidden(String ignore, @HiddenParameter SessionMemory memory){
        return memory != null;
    }

    @ReferenceGenerator
    public GeneratedVoidReference<List<String>> printf(String format){
        return l -> System.out.printf(format, l.toArray());
    }

    @ReferenceGenerator
    public GeneratedReference<Integer, Integer> pow(int power){
        return i -> (int) Math.pow(i, power);
    }

    @ReferenceGenerator(parameters = { List.class })
    public GeneratedPolyReference<String> join(String delimiter, int limit){
        return args -> ((List<?>) args[0]).stream().map(Object::toString).limit(limit).collect(Collectors.joining(delimiter));
    }

    @Reference
    public String concat(String f, boolean s){
        return f + s;
    }

    @Reference
    public boolean xor(boolean first, boolean second){
        return first ^ second;
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

    @Reference
    public int setNum(int num){
        return num;
    }

    @Reference
    public static int getTwo(){
        return 2;
    }

    @Reference
    public long getLongTwelve(){
        return 12L;
    }

    @Reference
    public long sum(Number f, Number s){
        return f.longValue() + s.longValue();
    }

    @Reference
    public String getNull(){
        return null;
    }

    @Reference
    public String consume(String s){
        return s + "A";
    }

    @Reference
    public boolean isNull(String s){
        return s == null;
    }
}