package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.LinkableElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.parser.XMLElementsParser;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Element(name = "link")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Link extends NodeElement {
    private String source;

    @Attribute(name = "src", nullable = false)
    private void linkSource(Node node, TranscriptionMemory memory, XMLElementsParser parser) {
        this.source = node.getAttributes().getNamedItem("src").getNodeValue();
        String[] sources = new String[]{this.source};

        if (source.endsWith("*")) {
            String path = source.substring(0, source.length() - 1);

            File directory = new File(path);
            if(!directory.isAbsolute())
                directory = new File(parser.getRootDirectory(), path);

            File[] files = directory.listFiles();
            if (files == null)
                throw new TranscriptionParsingException("Unable to find source '" + this.source + "'", node);

            files = Arrays.stream(files).filter(File::isFile).filter(f -> f.getName().endsWith(".xml")).toArray(File[]::new);
            sources = new String[files.length];
            for (int i = 0; i < files.length; i++)
                sources[i] = files[i].getAbsolutePath();
        }

        for (String source : sources)
            parseSource(source, node, memory, parser);
    }

    private void parseSource(String source, Node node, TranscriptionMemory memory, XMLElementsParser parser) {
        try {
            File file = new File(source).isAbsolute() ? new File(source) : new File(parser.getRootDirectory(), source);

            if (memory.getLinkedFiles().stream().anyMatch(f -> f.getAbsolutePath().equals(file.getAbsolutePath())))
                return;
            memory.getLinkedFiles().add(file);

            parser.setCurrentTree(null);
            Document document = XMLUtils.loadDocument(file);
            NodeElement result = parser.parse(document.getDocumentElement());

            if (!(result instanceof LinkableElement linkableElement))
                throw new TranscriptionParsingException("Unable to link element '" + result.getClass().getAnnotation(Element.class).name() + "' in '" + source + "'", node);

            if (linkableElement.afterParsedTask() != null)
                memory.getTasks().add(linkableElement.afterParsedTask());
        } catch (IOException e) {
            throw new TranscriptionParsingException("Unable to find source '" + source + "'", node);
        } catch (TranscriptionParsingException | TelegRiseRuntimeException | TelegRiseInternalException e){
            throw e;
        }catch (Exception e) {
            throw new TranscriptionParsingException("An exception occurred during parsing '" + source + "': " + e.getMessage(), node);
        }
    }
}
