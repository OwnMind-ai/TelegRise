package org.telegrise.telegrise.core.elements.head;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.parser.XMLElementsParser;
import org.w3c.dom.Node;

import java.io.File;

import static org.telegrise.telegrise.core.parser.XMLElementsParserTest.assertElements;
import static org.telegrise.telegrise.core.parser.XMLElementsParserTest.toNode;

class LinkTest {
    @Test
    void linkTest() throws Exception {
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(null,
                new ApplicationNamespace(this.getClass().getClassLoader(), "org.telegram.telegrise")),
                new File("src/test/resources/"));
        parser.load();

        Node linkNode = toNode("<link src=\"test/keyboards.xml\"/>");

        parser.parse(linkNode);

        Assertions.assertEquals("first", ((Keyboard) parser.getTranscriptionMemory().get("first")).getName());
        Assertions.assertEquals("second", ((Keyboard) parser.getTranscriptionMemory().get("second")).getName());

        Node keyboardNode = toNode("<keyboard byName=\"first\"/>");

        Keyboard result = (Keyboard) parser.parse(keyboardNode);
        parser.getTranscriptionMemory().getPendingFinalization().forEach(p -> {
            p.validate(parser.getTranscriptionMemory(), parser.getNamespace().getApplicationNamespace());
            p.load(parser.getTranscriptionMemory());
        });

        result.setByName(null);
        assertElements(parser.getTranscriptionMemory().get("first"), result, new ResourcePool());
    }
}