package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "bot")
@Data
@NoArgsConstructor
public final class BotTranscription implements TranscriptionElement {

    //TODO webhooks support
    @ElementField(name = "username", nullable = false)
    private String username;

    @ElementField(name = "token", nullable = false)
    private String token;

    @ElementField(name = "defaultParseMode")
    private String defaultParseMode = "html";

    @InnerElement(nullable = false)
    private List<Menu> menus;
}
