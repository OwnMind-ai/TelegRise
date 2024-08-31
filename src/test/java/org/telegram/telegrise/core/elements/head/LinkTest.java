package org.telegram.telegrise.core.elements.head;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.parser.XMLElementsParser;
import org.w3c.dom.Node;

import java.io.File;

import static org.telegram.telegrise.core.parser.XMLElementsParserTest.assertElements;
import static org.telegram.telegrise.core.parser.XMLElementsParserTest.toNode;

class LinkTest {
    @Test
    void linkTest() throws Exception {
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(), new File("src/test/resources/"));
        parser.load();

        Node linkNode = toNode("<link src=\"test/keyboards.xml\"/>");

        parser.parse(linkNode);

        Assertions.assertEquals("first", ((Keyboard) parser.getTranscriptionMemory().get("first")).getName());
        Assertions.assertEquals("second", ((Keyboard) parser.getTranscriptionMemory().get("second")).getName());

        Node keyboardNode = toNode("<keyboard byName=\"first\"/>");

        Keyboard result = (Keyboard) parser.parse(keyboardNode);
        result.setByName(null);
        assertElements(parser.getTranscriptionMemory().get("first"), result, new ResourcePool());
    }
}