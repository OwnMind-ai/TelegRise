package org.telegram.telegrise.core.elements.head;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;

@Element(name = "link")
@Data
@NoArgsConstructor
public class Link implements TranscriptionElement {
    private String source;

    @ElementField(name = "src", nullable = false)
    private void linkSource(Node node, ParserMemory memory, XMLElementsParser parser){
        this.source = node.getAttributes().getNamedItem("src").getNodeValue();

        try {
            Document document = XMLUtils.loadDocument(new File(this.source));
            TranscriptionElement result = parser.parse(document.getDocumentElement());  //FIXME fix scope restriction

            if (result instanceof StorableElement)
                ((StorableElement) result).store(memory);
            else
                throw new TranscriptionParsingException("Unable to link element '" + result.getClass().getAnnotation(Element.class).name() + "' in '" + this.source + "'", node);
        }  catch (IOException e) {
            throw new TranscriptionParsingException("Unable to find source '" + this.source + "'", node);
        } catch (Exception e) {
            throw new TranscriptionParsingException("An exception occurred during parsing '" + this.source + "': " + e.getMessage(), node);
        }
    }
}
