package org.telegrise.telegrise.core.elements.meta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

/**
 * Describes the options used for link preview generation.
 *
 * @see <a href="https://core.telegram.org/bots/api#linkpreviewoptions">Telegram API: LinkPreviewOptions</a>
 * @see Send
 */
@Element(name = "preview")
@Getter
@Setter
@NoArgsConstructor
public class LinkPreview extends NodeElement {
    private static final String PREFER_SMALL = "small";
    private static final String PREFER_LARGE = "large";

    /**
     * If true, the link preview will be disabled.
     */
    @Attribute(name = "disabled")
    private GeneratedValue<Boolean> disabled;

    /**
     * URL to use for the link preview. If empty, then the first URL found in the message text will be used.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * If 'small', the media in the link preview is supposed to be shrunk;
     * If 'large', the media in the link preview is supposed to be enlarged.
     */
    @Attribute(name = "prefer")
    private GeneratedValue<String> prefer;

    /**
     * If true, the link preview must be shown above the message text;
     * otherwise, the link preview will be shown below the message text.
     */
    @Attribute(name = "showAboveText")
    private GeneratedValue<Boolean> showAboveText;

    @Override
    public void validate(TranscriptionMemory memory, ApplicationNamespace namespace) {
        if (!prefer.validate(p -> PREFER_LARGE.equals(p) || PREFER_SMALL.equals(p)))
            throw new TranscriptionParsingException("Undefined 'prefer' value, must be 'small' or 'large'", node);
    }

    public LinkPreviewOptions producePreviewOptions(ResourcePool pool){
        String preferValue = GeneratedValue.generate(prefer, pool);
        return LinkPreviewOptions.builder()
                .isDisabled(GeneratedValue.generate(disabled, pool))
                .urlField(GeneratedValue.generate(url, pool))
                .preferLargeMedia(PREFER_LARGE.equals(preferValue) ? true : null)
                .preferSmallMedia(PREFER_SMALL.equals(preferValue) ? true : null)
                .showAboveText(GeneratedValue.generate(showAboveText, pool))
                .build();
    }
}