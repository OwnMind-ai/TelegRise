package org.telegram.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.elements.LinkableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;

@Element(name = "link")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Link implements TranscriptionElement {
    private String source;

    @Attribute(name = "src", nullable = false)
    private void linkSource(Node node, TranscriptionMemory memory, XMLElementsParser parser) {
        this.source = node.getAttributes().getNamedItem("src").getNodeValue();

        try {
            Document document = XMLUtils.loadDocument(new File(parser.getRootDirectory(), this.source));
            TranscriptionElement result = parser.parse(document.getDocumentElement());

            if (!(result instanceof LinkableElement))
                throw new TranscriptionParsingException("Unable to link element '" + result.getClass().getAnnotation(Element.class).name() + "' in '" + this.source + "'", node);

            LinkableElement linkableElement = (LinkableElement) result;
            if (linkableElement.afterParsedTask() != null)
                memory.getTasks().add(linkableElement.afterParsedTask());
        } catch (IOException e) {
            throw new TranscriptionParsingException("Unable to find source '" + this.source + "'", node);
        } catch (TranscriptionParsingException | TelegRiseRuntimeException e){
            throw e;
        }catch (Exception e) {
            throw new TranscriptionParsingException("An exception occurred during parsing '" + this.source + "': " + e.getMessage(), node);
        }
    }
}
