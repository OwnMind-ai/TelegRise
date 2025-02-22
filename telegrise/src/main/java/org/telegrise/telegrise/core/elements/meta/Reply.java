package org.telegrise.telegrise.core.elements.meta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;

/**
 * Describes reply parameters for the message that is being sent.
 * <pre>
 * {@code
 * <reply to="#messageId"/>
 * <reply to="#messageId">
 *     quote with <b>formatting</b>
 * </reply>
 * }
 * </pre>
 *
 * @since 0.11
 */
@Element(name = "reply")
@Getter @Setter @NoArgsConstructor
public class Reply extends NodeElement {
    /**
     * Identifier of the message that will be replied to in the current chat, or in the chat {@code chat_id} if it is specified.
     */
    @Attribute(name = "to", nullable = false)
    private GeneratedValue<Integer> to;

    /**
     * If the message to be replied to is from a different chat, unique identifier for the chat.
     */
    @Attribute(name = "fromChat")
    private GeneratedValue<Long> fromChat;

    /**
     * Set to true if the message should be sent even if the specified message to be replied to is not found.
     */
    @Attribute(name = "allowSendingWithoutReply")
    private GeneratedValue<Boolean> allowSendingWithoutReply;

    /**
     * Position of the quote in the original message in UTF-16 code units.
     */
    @Attribute(name = "quotePosition")
    private GeneratedValue<Integer> quotePosition;

    @InnerElement
    private Text quote;

    public ReplyParameters produceReplyParameters(ResourcePool pool){
        return ReplyParameters.builder()
                .messageId(to.generate(pool))
                .chatId(GeneratedValue.generateOptional(fromChat, pool).map(String::valueOf).orElse(null))
                .allowSendingWithoutReply(GeneratedValue.generate(allowSendingWithoutReply, pool))
                .quotePosition(GeneratedValue.generate(quotePosition, pool))
                .quote(quote != null ? quote.generateText(pool) : null)
                .quoteEntities(quote != null ? GeneratedValue.generateOptional(quote.getEntities(), pool).orElse(List.of()) : List.of())
                .quoteParseMode(quote != null ? quote.getParseMode().generate(pool) : null)
                .build();
    }
}
