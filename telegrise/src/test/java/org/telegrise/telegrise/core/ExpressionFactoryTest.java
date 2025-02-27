package org.telegrise.telegrise.core;

import org.junit.jupiter.api.Test;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.telegrise.telegrise.core.parser.XMLElementsParserTest.toNode;

public class ExpressionFactoryTest {

    @Test
    void createExpression() {
        String expression = "Some text and ${controller.getNum()} generated";
        Node node = toNode("<tag expression=\"" + expression + "\"/>");

        LocalNamespace namespace = new LocalNamespace(this.getClass(), new ApplicationNamespace(this.getClass().getClassLoader(), ""));
        ResourcePool pool = new ResourcePool(null, this, null, null, null);
        assertEquals("Some text and 12 generated", ExpressionFactory.createExpression(expression, String.class, node, namespace).generate(pool));
    }

    @SuppressWarnings("unused")
    public int getNum(){
        return 12;
    }
}