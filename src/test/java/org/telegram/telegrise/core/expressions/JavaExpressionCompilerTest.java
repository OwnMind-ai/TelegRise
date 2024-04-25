package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

public class JavaExpressionCompilerTest {

    @Test
    void parse() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        JavaExpressionCompiler parser = new JavaExpressionCompiler(JavaExpressionCompiler.getTempDirectory());
        String expression = "controller.getData(update.getCallbackQuery())";
        LocalNamespace namespace = new LocalNamespace(this.getClass(), new ApplicationNamespace(this.getClass().getClassLoader()));
        ResourcePool resourcePool = new ResourcePool(new Update(), this, null, null);
        resourcePool.getUpdate().setCallbackQuery(new CallbackQuery("", null, null, "", "data", "", ""));

        assertEquals("data", parser.compile(expression, namespace, String.class, toNode("<tag expression=\"${" + expression + "}\"/>")).generate(resourcePool));

        expression = "controller.getData(update.getCallbackQuery()) + #reference";
        String finalExpression = expression;
        assertThrows(TranscriptionParsingException.class, () -> parser.compile(finalExpression, namespace, String.class, toNode("<tag expression=\"${" + finalExpression + "}\"/>")).generate(resourcePool));

        expression = "controller(update.getCallbackQuery())";
        String finalExpression1 = expression;
        assertThrows(TranscriptionParsingException.class, () -> parser.compile(finalExpression1, namespace, String.class, toNode("<tag expression=\"${" + finalExpression1 + "}\"/>")).generate(resourcePool));
    }

    @SuppressWarnings("unused")
    public String getData(CallbackQuery query){
        return query.getData();
    }
}