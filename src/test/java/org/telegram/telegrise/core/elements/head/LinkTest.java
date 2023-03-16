package org.telegram.telegrise.core.elements.head;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.parser.XMLElementsParser;
import org.w3c.dom.Node;

import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

class LinkTest {
    @Test
    void linkTest() throws Exception {
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace());
        parser.load();

        Node linkNode = toNode("<link src=\"src/test/resources/keyboards.xml\"/>");

        parser.parse(linkNode);

        Assertions.assertEquals("first", ((Keyboard) parser.getParserMemory().get("first")).getName());
        Assertions.assertEquals("second", ((Keyboard) parser.getParserMemory().get("second")).getName());
    }
}