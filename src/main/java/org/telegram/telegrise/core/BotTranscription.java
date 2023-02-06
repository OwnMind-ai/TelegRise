package org.telegram.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrise.core.elements.Menu;
import org.w3c.dom.Node;

import java.util.List;

public final class BotTranscription {
    public static BotTranscription ofRootNode(Node node) throws TranscriptionParsingException{
        var result = new BotTranscription(
                getOrThrow(node, "username"),
                getOrThrow(node, "token")
        );

        if (node.getAttributes().getNamedItem("defaultParseMode") != null)
            result.defaultParseMode = node.getAttributes().getNamedItem("defaultParseMode").getNodeValue();

        return result;
    }

    private static String getOrThrow(Node node, String attributeName) throws TranscriptionParsingException{
        Node attribute = node.getAttributes().getNamedItem(attributeName);

        if (attribute == null)
            throw new TranscriptionParsingException("Field \"" + attributeName + "\" can't be null", node);

        return attribute.getNodeValue();
    }

    //TODO webhooks support
    private final String username;
    private final String token;
    private String defaultParseMode = "html";

    @Setter @Getter
    private List<Menu> menus;

    public BotTranscription(String username, String token) {
        this.username = username;
        this.token = token;
    }

}
