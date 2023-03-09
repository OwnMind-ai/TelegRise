package org.telegram.telegrise.core.elements.media;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.elements.actions.Send;

public interface MediaType extends TranscriptionElement {
    PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool);
    InputMedia createInputMedia(Send parent, ResourcePool pool);
}
