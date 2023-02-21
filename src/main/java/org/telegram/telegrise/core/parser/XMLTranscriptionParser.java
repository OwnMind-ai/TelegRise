package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLTranscriptionParser implements TranscriptionParser{
    private final Document document;
    private final XMLElementsParser elementsParser;

    private final ClassLoader classLoader;

    public XMLTranscriptionParser(Document document, XMLElementsParser elementsParser, ClassLoader classLoader) {
        this.document = document;
        this.elementsParser = elementsParser;
        this.classLoader = classLoader;
    }

    public BotTranscription parse() throws Exception {
        this.processInstructions(XMLUtils.getInstructions(document));
        return (BotTranscription) elementsParser.parse(document.getElementsByTagName(
                    BotTranscription.class.getAnnotation(Element.class).name()).item(0));
    }

    private void processInstructions(Node[] instructions){

    }
}
