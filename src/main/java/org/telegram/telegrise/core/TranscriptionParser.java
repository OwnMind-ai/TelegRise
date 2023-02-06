package org.telegram.telegrise.core;

import org.telegram.telegrise.core.parser.ElementsParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TranscriptionParser {
    private final Document document;
    private final ElementsParser elementsParser;

    public TranscriptionParser(Document document, ElementsParser elementsParser) {
        this.document = document;
        this.elementsParser = elementsParser;
    }

    public BotTranscription parse(){
        Node rootNode = document.get
        BotTranscription result = BotTranscription.ofRootNode()
    }
}
