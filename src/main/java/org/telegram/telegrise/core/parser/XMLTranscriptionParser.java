package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLTranscriptionParser implements TranscriptionParser{
    private final Document document;
    private final XMLElementsParser elementsParser;
    private final ApplicationNamespace applicationNamespace;

    public XMLTranscriptionParser(Document document, XMLElementsParser elementsParser, ClassLoader classLoader) {
        this.document = document;
        this.elementsParser = elementsParser;
        this.applicationNamespace = new ApplicationNamespace(classLoader);
        this.elementsParser.setNamespace(this.applicationNamespace.emptyLocal());
    }

    public BotTranscription parse() throws Exception {
        this.processInstructions(XMLUtils.getInstructions(document));

        BotTranscription result = (BotTranscription) elementsParser.parse(document.getElementsByTagName(
                    BotTranscription.class.getAnnotation(Element.class).name()).item(0));

        this.elementsParser.getTranscriptionMemory().setReadOnly();
        result.setMemory(this.elementsParser.getTranscriptionMemory());
        this.elementsParser.getTranscriptionMemory().getTasks().forEach(t -> t.accept(result));
        this.elementsParser.getTranscriptionMemory().getTasks().clear();

        this.elementsParser.getTranscriptionMemory().getPendingValidation()
                .forEach(p -> p.getLeft().validate(p.getRight(), this.elementsParser.getTranscriptionMemory()));

        return result;
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
            throw new TranscriptionParsingException("Unable to import class '" + node.getNodeValue() + "': Class not found", node.getParentNode());
        }
    }
}
