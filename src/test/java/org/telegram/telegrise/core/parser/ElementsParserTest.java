package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.elements.Text;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ElementsParserTest {

    public static Node toNode(String node){
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(node.getBytes(StandardCharsets.UTF_8)))
                    .getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void parse() throws Exception {
        ElementsParser parser = new ElementsParser();
        parser.load();

        Node node = toNode("<text parseMode=\"html\">val</text>");

        assertText(new Text("val", "html"), (Text) parser.parse(node));
    }

    public static void assertText(Text expected, Text actual){
        assertTrue(
                expected.getParseMode().equalsTo(actual.getParseMode(), null)
                        && expected.getText().equalsTo(actual.getText(),null)
        );
    }
}