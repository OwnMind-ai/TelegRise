package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.head.HeadBlock;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;

@Element(name = "bot")
@Data
@NoArgsConstructor
public final class BotTranscription implements TranscriptionElement {

    //TODO webhooks support
    @Attribute(name = "username", nullable = false)
    private String username;

    @Attribute(name = "token", nullable = false)
    private String token;

    @Attribute(name = "defaultParseMode")
    private String defaultParseMode = "html";

    @InnerElement(nullable = false)
    private Menu rootMenu;

    @InnerElement(priority = 10)
    private HeadBlock head;
}
