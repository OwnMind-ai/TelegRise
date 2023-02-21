package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

public class ExpressionParserTest {

    @Test
    void parse() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ExpressionParser parser = new ExpressionParser(ExpressionParser.getTempDirectory());
        String expression = "handler.getData(update.getCallbackQuery())";
        LocalNamespace namespace = new LocalNamespace(this.getClass(), new ApplicationNamespace(this.getClass().getClassLoader()));
        ResourcePool resourcePool = new ResourcePool(new Update(), this);
        resourcePool.getUpdate().setCallbackQuery(new CallbackQuery("", null, null, "", "data", "", ""));

        assertEquals("data", parser.parse(expression, namespace, String.class, toNode("<tag expression=\"${" + expression + "}\"/>")).generate(resourcePool));

        expression = "handler.getData(update.getCallbackQuery()) + #reference";
        String finalExpression = expression;
        assertThrows(TranscriptionParsingException.class, () -> parser.parse(finalExpression, namespace, String.class, toNode("<tag expression=\"${" + finalExpression + "}\"/>")).generate(resourcePool));

        expression = "handler(update.getCallbackQuery())";
        String finalExpression1 = expression;
        assertThrows(TranscriptionParsingException.class, () -> parser.parse(finalExpression1, namespace, String.class, toNode("<tag expression=\"${" + finalExpression1 + "}\"/>")).generate(resourcePool));
    }

    @SuppressWarnings("unused")
    public String getData(CallbackQuery query){
        return query.getData();
    }
}