package org.telegram.telegrise.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrise.core.elements.Menu;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "bot")
@NoArgsConstructor
public final class BotTranscription implements TranscriptionElement {

    //TODO webhooks support
    @ElementField(name = "defaultParseMode", nullable = false)
    private String username;

    @ElementField(name = "defaultParseMode", nullable = false)
    private String token;

    @ElementField(name = "defaultParseMode")
    private String defaultParseMode = "html";

    @InnerElement(nullable = false)
    @Setter @Getter
    private List<Menu> menus;
}
