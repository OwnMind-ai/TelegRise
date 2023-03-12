package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.TreeHandler;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.elements.media.Photo;
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
    private Update update;

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

        this.update = new Update();
        this.update.setMessage(new Message());
        this.update.getMessage().setChat(new Chat(-1L, "chat"));
        this.update.getMessage().setFrom(new User(-2L, "First Name", false));
    }

    @Test
    void parse() {
        Photo photo = new Photo();
        photo.setFileId(GeneratedValue.ofValue("id"));

        Send expectedSendFolded = new Send();
        expectedSendFolded.setText(new Text("<b>Bye</b>", "html"));
        expectedSendFolded.setMedias(List.of(photo));
        expectedSendFolded.setChatId(GeneratedValue.ofValue(update.getMessage().getChatId()));

        Branch expectedBranchFolded = new Branch();
        expectedBranchFolded.setWhen(GeneratedValue.ofValue(true));
        expectedBranchFolded.setActions(List.of(expectedSendFolded));

        Send expectedSend = new Send();
        expectedSend.setText(new Text(
                "Hi, " + update.getMessage().getFrom().getFirstName(),
                "html"
        ));
        expectedSend.setChatId(GeneratedValue.ofValue(update.getMessage().getChatId()));

        Branch expectedBranch = new Branch();
        expectedBranch.setWhen(GeneratedValue.ofValue(update.hasMessage()));
        expectedBranch.setActions(List.of(expectedSend));
        expectedBranch.setBranches(List.of(expectedBranchFolded));

        Send treeSend = new Send();
        treeSend.setText(new Text("Text", "markdown"));

        Tree expectedTree = new Tree();
        expectedTree.setName("name");
        expectedTree.setCallbackTriggers(new String[]{"callback-data"});
        expectedTree.setKeys(new String[]{"first", "second"});
        expectedTree.setCommands(new String[]{"example"});
        expectedTree.setHandler(this.getClass());
        expectedTree.setActions(List.of(treeSend));
        expectedTree.setBranches(List.of(expectedBranch));

        Menu expectedMenu = new Menu();
        expectedMenu.setName("Main");
        expectedMenu.setActions(null);
        expectedMenu.setTrees(List.of(expectedTree));

        BotTranscription transcription = new BotTranscription();
        transcription.setUsername("bot");
        transcription.setToken("token");
        transcription.setRootMenu(expectedMenu);

        assertElements(transcription, this.transcription, new ResourcePool(update, this));
    }

    @Reference
    private boolean predicate(Update update){
        return true;
    }
}