package org.telegram.telegrise.core.elements.media;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.Collections;
import java.util.List;

@Element(name = "media")
@Getter @Setter @NoArgsConstructor
public class Media extends MediaType{
    @Attribute(name = "sendMethod")
    private GeneratedValue<PartialBotApiMethod<?>> sendMethod;

    @Attribute(name = "inputMedia")
    private GeneratedValue<InputMedia> inputMedia;

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
