package org.telegrise.telegrise.core.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.Branch;
import org.telegrise.telegrise.core.elements.Root;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.elements.head.HeadBlock;
import org.telegrise.telegrise.core.elements.head.Link;
import org.telegrise.telegrise.core.elements.keyboard.Button;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.keyboard.Row;
import org.telegrise.telegrise.core.elements.media.Photo;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

import static org.telegrise.telegrise.core.parser.XMLElementsParserTest.assertElements;

@TreeController
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class XMLTranscriptionParserTest {

    private BotTranscription transcription;
    private Update update;

    @BeforeAll
    void before() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File("src/test/resources/parseTestSample.xml"));
        document.getDocumentElement().normalize();

        var elementParser = new XMLElementsParser(
                new LocalNamespace(null, new ApplicationNamespace(this.getClass().getClassLoader(), "")),
                new File("src/test/resources/")
        );

        elementParser.load();
        XMLTranscriptionParser parser = new XMLTranscriptionParser(document, elementParser);

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

        Keyboard keyboard = new Keyboard();
        keyboard.setName("first");
        keyboard.setType("inline");
        keyboard.setByName(keyboard.getName());

        keyboard.setRows(List.of(
                new Row(List.of(new Button("First", "first"))),
                new Row(List.of(new Button("Second", "second")))
        ));

        Send expectedSend = new Send();
        expectedSend.setText(new Text(
                "Hi, " + update.getMessage().getFrom().getFirstName() + "\n\n1\n2",
                "html"
        ));
        expectedSend.setKeyboard(keyboard);
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
        expectedTree.setController(this.getClass());
        expectedTree.setActions(List.of(treeSend));
        expectedTree.setBranches(List.of(expectedBranch));

        Root expectedRoot = new Root();
        expectedRoot.setName("Main");
        expectedRoot.setActions(null);
        expectedRoot.setTrees(List.of(expectedTree));

        BotTranscription transcription = new BotTranscription();
        transcription.setHead(new HeadBlock(List.of(new Link("test/keyboards.xml")), null, null, null, null, null));
        transcription.setUsername(GeneratedValue.ofValue("bot"));
        transcription.setToken(GeneratedValue.ofValue("token"));
        transcription.setSessionType("chat");
        transcription.setRoot(expectedRoot);

        assertElements(transcription, this.transcription, new ResourcePool(update, this, null, null));
    }

    @Reference
    public boolean predicate(Update update){
        return true;
    }
}