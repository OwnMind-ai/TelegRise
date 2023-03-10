package org.telegram.telegrise.core.elements.media;

import org.telegram.telegrambots.meta.api.methods.send.SendMediaBotMethod;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.elements.actions.Send;

import java.util.List;

public interface MediaType extends TranscriptionElement {
    SendMediaBotMethod<?> createSender(Send parent, ResourcePool pool);
    List<InputMedia> createInputMedia(Send parent, ResourcePool pool);
}
