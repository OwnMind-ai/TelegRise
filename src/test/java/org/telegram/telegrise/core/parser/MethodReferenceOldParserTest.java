package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.expressions.MethodReferenceParser;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodReferenceOldParserTest {

    @Test
    void parseInstanceReference() {
        Node node = XMLElementsParserTest.toNode("<tag methods=\"#first -> #second -> #third ; #another\"/>");

        var instance = new MethodReferenceOldParserTest();
        GeneratedValue<String> result = MethodReferenceParser.parse("#first -> #second -> #third ; #another", new LocalNamespace(instance.getClass(), null), node)[0].toGeneratedValue(String.class, node);

        assertEquals("144.0", result.generate(new ResourcePool(new Update(), instance, null, null)));
    }

    @SuppressWarnings("unused")
    @Reference
    private boolean first(){
        return true;
    }

    @SuppressWarnings("unused")
    @Reference
    private double second(boolean value){
        return Math.pow(12, 2);
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