package org.telegrise.telegrise.core.parser;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.expressions.Syntax;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class XMLTranscriptionParser implements TranscriptionParser{
    private static final Logger logger = LoggerFactory.getLogger(XMLTranscriptionParser.class);

    private final Document document;
    private final XMLElementsParser elementsParser;
    private final ApplicationNamespace applicationNamespace;

    public XMLTranscriptionParser(Document document, XMLElementsParser elementsParser) {
        this.document = document;
        this.elementsParser = elementsParser;
        this.applicationNamespace = elementsParser.getNamespace().getApplicationNamespace();
        this.elementsParser.setNamespace(this.applicationNamespace.emptyLocal());
    }

    public BotTranscription parse() throws Exception {
        long startMillis = System.currentTimeMillis();
        this.processInstructions(XMLUtils.getInstructions(document));
        this.processAutoImport();

        BotTranscription result = (BotTranscription) elementsParser.parse(document.getElementsByTagName(
                    BotTranscription.class.getAnnotation(Element.class).name()).item(0));

        this.elementsParser.getTranscriptionMemory().setReadOnly();
        result.setMemory(this.elementsParser.getTranscriptionMemory());
        this.elementsParser.getTranscriptionMemory().getTasks().forEach(t -> t.accept(result));
        this.elementsParser.getTranscriptionMemory().getTasks().clear();

        this.calculateTreeNodesLevel(result);

        this.elementsParser.getTranscriptionMemory().getPendingFinalization()
                .forEach(p -> {
                    p.validate(this.elementsParser.getTranscriptionMemory(), elementsParser.getNamespace().getApplicationNamespace());
                    p.load(this.elementsParser.getTranscriptionMemory());
                });

        //noinspection SpellCheckingInspection
        logger.info("Transcription parsed in {}", DurationFormatUtils.formatDuration(
                System.currentTimeMillis() - startMillis, "s's 'S'ms'"
        ));

        return result;
    }

    private void processAutoImport() {
        Set<Class<?>> controllers = new Reflections(applicationNamespace.getApplicationPackageName()).getTypesAnnotatedWith(TreeController.class);

        controllers.stream().filter(c -> c.getAnnotation(TreeController.class).autoImport())
                .forEach(this.applicationNamespace::addClass);
    }

    private void processInstructions(Node[] instructions){
        for (Node node : instructions) {
            if (node.getNodeName().equals(Syntax.IMPORT)) {
                this.processImport(node);
            } else {
                throw new TranscriptionParsingException("Unknown instruction: <? " + node.getNodeName() + " " + node.getNodeValue() + " ?>", node);
            }
        }
    }

    private void processImport(Node node){
        try {
            this.applicationNamespace.addClass(node.getNodeValue().trim());
        } catch (ClassNotFoundException e) {
            throw new TranscriptionParsingException("Unable to import class '" + node.getNodeValue() + "': Class not found", node);
        }
    }

    private void calculateTreeNodesLevel(BotTranscription result) {
        result.getRoot().setLevel(0);

        Queue<BranchingElement> queue = new LinkedList<>();
        queue.add(result.getRoot());

        while (!queue.isEmpty()){
            var element = queue.poll();

            if (element.getChildren() != null)
                element.getChildren().forEach(e -> {
                    e.setLevel(element.getLevel() + 1);
                    queue.add(e);
                });
        }
    }
}
