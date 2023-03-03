package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.TreeHandler;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.elements.actions.Send;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

import static org.telegram.telegrise.core.parser.XMLElementsParserTest.assertElements;

@TreeHandler
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class XMLTranscriptionParserTest {

    private BotTranscription transcription;

    @BeforeAll
    void before() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File("src/test/resources/sample.xml"));
        document.getDocumentElement().normalize();

        var elementParser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(this.getClass().getClassLoader())));
        elementParser.load();
        XMLTranscriptionParser parser = new XMLTranscriptionParser(document, elementParser, this.getClass().getClassLoader());

        this.transcription = parser.parse();
    }

    @Test
    void parse() {
        Send expectedSend = new Send();
        expectedSend.setText(new Text("Text", "html"));
        expectedSend.setChatId(GeneratedValue.ofValue(-1L));

        Branch expectedBranch = new Branch();
        expectedBranch.setWhen(GeneratedValue.ofValue(true));
        expectedBranch.setActions(List.of(expectedSend, expectedSend));

        Tree expectedTree = new Tree();
        expectedTree.setName("name");
        expectedTree.setCallbackTriggers(new String[]{"callback-data"});
        expectedTree.setKeys(new String[]{"first", "second"});
        expectedTree.setCommands(new String[]{"example"});
        expectedTree.setHandler(this.getClass());
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
        transcription.setRootMenu(expectedMenu);

        assertElements(transcription, this.transcription, new ResourcePool(null, this));
    }

    @Reference
    private boolean predicate(Update update){
        return true;
    }
}