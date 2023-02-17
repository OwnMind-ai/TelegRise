package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.BotTranscription;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLTranscriptionParser implements TranscriptionParser{
    private final Document document;
    private final XMLElementsParser elementsParser;

    public XMLTranscriptionParser(Document document, XMLElementsParser elementsParser) {
        this.document = document;
        this.elementsParser = elementsParser;
    }

    public BotTranscription parse() throws Exception {
        return (BotTranscription) elementsParser.parse(document.getDocumentElement());
    }
}
