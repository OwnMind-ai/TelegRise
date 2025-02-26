package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.security.Roles;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;

/**
 * This element stores bot configurations and transcription metadata.
 *
 * @since 0.1
 */
@Element(name = "head")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HeadBlock extends NodeElement {
    @InnerElement(priority = 1)
    private List<Link> links;

    @InnerElement
    private Token token;

    @InnerElement
    private SessionType sessionType;

    @InnerElement
    private Webhook webhook;

    @InnerElement
    private TelegramUrl telegramUrl;

    @InnerElement(priority = -1)
    private Roles roles;
}
