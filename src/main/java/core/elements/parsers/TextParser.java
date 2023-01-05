package core.elements.parsers;

import core.TranscriptionParsingException;
import core.elements.Text;
import core.utils.XMLUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

public class TextParser implements ElementParser<Text> {
    public static String getParseMode(Node node) throws TranscriptionParsingException {
        Node value = node.getAttributes().getNamedItem("parseMode");

        if (value == null) return null;

        switch (value.getNodeValue()){
            case "html": case "markdown": return value.getNodeValue();
            default:
                throw new TranscriptionParsingException("Unknown parse mode: " + value, node);
        }
    }

    @Override
    public @NotNull String getElementName() {
        return "text";
    }

    @Override
    public Text parse(Node node) throws TranscriptionParsingException {
        String parse = getParseMode(node);
        return new Text(XMLUtils.innerXML(node), parse);
    }
}
