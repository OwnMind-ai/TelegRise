package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.Menu;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.Send;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

import static org.telegram.telegrise.core.parser.XMLElementsParserTest.assertElements;

public class XMLTranscriptionParserTest {

    @Test
    void parse() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File("src/test/resources/sample.xml"));
        document.getDocumentElement().normalize();

        var elementParser = new XMLElementsParser();
        elementParser.load();
        XMLTranscriptionParser parser = new XMLTranscriptionParser(document, elementParser, this.getClass().getClassLoader());

        Send expectedSend = new Send();
        expectedSend.setText(new Text("Text", "html"));
        expectedSend.setChatId(GeneratedValue.ofValue(-1L));

        Branch expectedBranch = new Branch();
        expectedBranch.setWhen(GeneratedValue.ofValue(true));
        expectedBranch.setActions(List.of(expectedSend, expectedSend));

        Tree expectedTree = new Tree();
        expectedTree.setName("name");
        expectedTree.setPredicate(GeneratedValue.ofValue(true));
        expectedTree.setCallbackTriggers(new String[]{"callback-data"});
        expectedTree.setKeys(new String[]{"first", "second"});
        expectedTree.setCommands(new String[]{"example"});
        expectedTree.setHandlerName("XMLTranscriptionParserTest");
        expectedTree.setType("reply");
        expectedTree.setText(new Text("Text", "markdown"));
        expectedTree.setBranches(List.of(expectedBranch));

        Menu expectedMenu = new Menu();
        expectedMenu.setName("Main");
        expectedMenu.setType("reply");
        expectedMenu.setTrees(List.of(expectedTree));

        BotTranscription transcription = new BotTranscription();
        transcription.setUsername("bot");
        transcription.setToken("token");
        transcription.setMenus(List.of(expectedMenu));

        assertElements(transcription, parser.parse());
    }
}