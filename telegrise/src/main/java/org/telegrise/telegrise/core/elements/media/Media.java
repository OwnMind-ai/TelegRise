package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.Collections;
import java.util.List;

/**
 * Use this element to specify a general media object.
 * This element can be used in media groups or by themselves, if {@code sendMethod} is specified.
 *
 * @since 0.1
 */
@Element(name = "media")
@Getter @Setter @NoArgsConstructor
public class Media extends MediaType{
    /**
     * Send a method provider that will be executed if this media element is the only one
     */
    @Attribute(name = "sendMethod")
    private GeneratedValue<PartialBotApiMethod<?>> sendMethod;

    /**
     * Input media of this media element
     */
    @Attribute(name = "inputMedia")
    private GeneratedValue<InputMedia> inputMedia;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        if (sendMethod == null)
            throw new TelegRiseRuntimeException("Unable to send media: send method is null", node);

        pool.addComponent(parent);
        return sendMethod.generate(pool);
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        return Collections.singletonList(inputMedia.generate(pool));
    }

    @Override
    public GeneratedValue<String> getFileId() {
        return null;
    }

    @Override
    public boolean isGroupable() {
        return inputMedia != null;
    }

    @Override
    public GeneratedValue<String> getUrl() {
        return null;
    }

    @Override
    public GeneratedValue<InputFile> getInputFile() {
        return null;
    }

    @Override
    public boolean isMediaRequired() {
        return false;
    }
}
