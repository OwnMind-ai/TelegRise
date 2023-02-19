package org.telegram.telegrise.core.expressions;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ResourcePool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionParserTest {

    @Test
    void parse() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ExpressionParser parser = new ExpressionParser(System.getProperty("user.dir"));
        String expression = "handler.getData(update.getCallbackQuery())";
        ResourcePool resourcePool = new ResourcePool(new Update(), this);
        resourcePool.getUpdate().setCallbackQuery(new CallbackQuery("", null, null, "", "data", "", ""));

        assertEquals("data", parser.parse(expression, resourcePool, String.class).generate(resourcePool));
    }

    @SuppressWarnings("unused")
    public String getData(CallbackQuery query){
        return query.getData();
    }
}