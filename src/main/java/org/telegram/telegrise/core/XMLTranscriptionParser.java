package org.telegram.telegrise.core;

import org.telegram.telegrise.core.parser.ElementsParser;
import org.w3c.dom.Document;

public class XMLTranscriptionParser {
    private final Document document;
    private final ElementsParser elementsParser;

    public XMLTranscriptionParser(Document document, ElementsParser elementsParser) {
        this.document = document;
        this.elementsParser = elementsParser;
    }

    public BotTranscription parse(){
        return null;
    }
}
