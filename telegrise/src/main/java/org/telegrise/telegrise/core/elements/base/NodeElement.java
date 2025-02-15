package org.telegrise.telegrise.core.elements.base;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializer;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The base class for all elements that can be parsed from XML <b>transcriptions</b>. 
 * 
 * @since 0.6.4
 */
public abstract class NodeElement implements Serializable {
    /**
     * Actual node instance of this element.
     */
    protected transient Node node;

    /**
     * Tree in which element resides, if any.
     */
    @Getter @Setter
    protected Tree parentTree;
    
    /**
     * Parsed parent element ({@code node.getParentNode()})
     */
    @Getter @Setter
    protected NodeElement parent;

    /**
     * If the implementation returns not null, the transcription parser will assume the returned namespace as current. 
     * 
     * @param global application namespace
     * @return new namespace or null
     */
    public LocalNamespace createNamespace(ApplicationNamespace global){
        return null;
    }

    /**
     * Validates a parsed element and its content.
     * Implementation must be used only for this purpose and does not change the state of the element.
     * @param memory transcription memory
     * @param namespace application namespace
     */
    public void validate(TranscriptionMemory memory, ApplicationNamespace namespace){
        validate(memory);
    }

    /**
     * Validates a parsed element and its contents.
     * Implementation must be used only for this purpose and does not change the state of the element.
     * This method is called <b>before</b> {@link #load(TranscriptionMemory)}.
     * @param memory transcription memory
     */
    @Contract(pure = true)
    protected void validate(TranscriptionMemory memory){}

    /**
     * Loads and mutates this element.
     * Implementation can use this method
     * to load the necessary resources from transaction memory or do any other mutation.
     * This method <b>can</b> throw exceptions instead of {@code validate} if required
     * and is called <b>after</b> {@link #validate(TranscriptionMemory, ApplicationNamespace)}.
     * @param memory transcription memory
     */
    @Contract
    public void load(TranscriptionMemory memory){}

    protected final <T> T generateNullableProperty(GeneratedValue<T> property, ResourcePool pool){
        return GeneratedValue.generate(property, pool);
    }

    protected final <T> T generateNullableProperty(GeneratedValue<T> property, T orElse, ResourcePool pool){
        return GeneratedValue.generate(property, pool, orElse);
    }

    /**
     * Converts a node to a good-looking string representation. Used for exceptions.
     * @param node a node to convert
     * @return beautiful string
     */
    public static @NotNull String formatNode(Node node) {
        StringBuilder builder = new StringBuilder();

        Document document = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
        if (document == null || document.getDocumentURI() == null){
            builder.append("<unknown source>: ");
        } else {
            try {
                builder.append(new URI(document.getDocumentURI()).toURL().getFile()).append(": ");
            } catch (MalformedURLException | URISyntaxException e) {
                builder.append(node.getOwnerDocument().getDocumentURI()).append(": ");
            }
        }

        List<String> path = new ArrayList<>();
        traversePath(path, node.getParentNode());
        builder.append(String.join(".", path));

        LSSerializer serializer = XMLUtils.getLsSerializer(node);
        builder.append("\n").append(serializer.writeToString(node).split("\n")[0]);

        return builder.toString();
    }

    private static void traversePath(List<String> path, Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) return;

        String s = node.getNodeName();
        if (node.getAttributes().getNamedItem("name") != null)
            s += "['" + node.getAttributes().getNamedItem("name").getNodeValue() + "']";

        path.addFirst(s);
        traversePath(path, node.getParentNode());
    }

    public final void setElementNode(Node node){
        this.node = node;
    }

    public final Node getElementNode(){
        return node;
    }

    @Override
    public int hashCode(){
        Object[] hash = new Object[1 + node.getAttributes().getLength() * 2];
        hash[0] = node.getNodeName();
        for(int i = 0; i < node.getAttributes().getLength(); i++){
            var attr = node.getAttributes().item(i);
            hash[i * 2 + 1] = attr.getNodeName();
            hash[i * 2 + 2] = attr.getNodeValue();
        }

        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return NodeElement.formatNode(node);
    }
}
