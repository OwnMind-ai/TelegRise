package core.elements.parsers;

import org.telegram.telegrise.core.TranscriptionParsingException;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.parsers.TextParser;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TextParserTest {
    public static Node toNode(String node){
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(node.getBytes(StandardCharsets.UTF_8)))
                    .getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @org.junit.jupiter.api.Test
    void getParseMode() throws TranscriptionParsingException {
        Node node = toNode("<text parseMode=\"html\">val</text>");
        assertEquals("html", TextParser.getParseMode(node));

        node = toNode("<text>val</text>");
        assertNull(TextParser.getParseMode(node));

        node = toNode("<text parseMode=\"wrong\">val</text>");
        Node finalNode = node;
        assertThrows(TranscriptionParsingException.class, () -> TextParser.getParseMode(finalNode));
    }

    @org.junit.jupiter.api.Test
    void parse() throws TranscriptionParsingException {
        TextParser parser = new TextParser();

        Node node = toNode("<text parseMode=\"html\">val</text>");
        assertText(new Text("val", "html"), parser.parse(node));

        node = toNode("<text>v<b>a</b>l</text>");
        assertText(new Text("v<b>a</b>l", null), parser.parse(node));
    }

    public void assertText(Text expected, Text actual){
        assertTrue(
                expected.getText().equalsTo(actual.getText(), null, null)
                && expected.getText().equalsTo(actual.getText(),null, null)
        );
    }
}