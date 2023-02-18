package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.ResourcePool;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class MethodReferenceParserTest {

    @Test
    void parse() throws InvocationTargetException, IllegalAccessException {
        Node node = XMLElementsParserTest.toNode("<tag methods=\"#first -> #second -> #third ; #another\"/>");

        var instance = new MethodReferenceParserTest();
        var result = MethodReferenceParser.parse("#first -> #second -> #third ; #another", instance.getClass(), node);

        assertEquals("144.0", result[0].invoke(instance, new ResourcePool(new Update())));
    }

    @SuppressWarnings("unused")
    @Reference
    private int first(){
        return 12;
    }

    @SuppressWarnings("unused")
    @Reference
    private double second(int value){
        return Math.pow(value, 2);
    }

    @SuppressWarnings("unused")
    @Reference
    private String third(double next){
        return String.valueOf(next);
    }

    @SuppressWarnings("unused")
    @Reference
    private void another(Update update){
        System.out.println(update);
    }
}