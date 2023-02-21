package org.telegram.telegrise.core;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.*;
import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

public class ExpressionFactoryTest {

    @Test
    void createExpression() {
        String expression = "Some text and ${handler.getNum()} generated";
        Node node = toNode("<tag expression=\"" + expression + "\"/>");

        LocalNamespace namespace = new LocalNamespace(this.getClass(), new ApplicationNamespace(this.getClass().getClassLoader()));
        ResourcePool pool = new ResourcePool(null, this);
        assertEquals("Some text and 12 generated", ExpressionFactory.createExpression(expression, String.class, node, namespace).generate(pool));
    }

    @SuppressWarnings("unused")
    public int getNum(){
        return 12;
    }
}