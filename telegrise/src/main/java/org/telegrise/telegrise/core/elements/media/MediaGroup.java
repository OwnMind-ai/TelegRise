package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.List;

@Element(name = "mediaGroup")
@Getter @Setter @NoArgsConstructor
public class MediaGroup extends MediaType{
    @Attribute(name = "inputMedia", nullable = false)
    private GeneratedValue<List<InputMedia>> inputMedia;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        List<InputMedia> media = inputMedia.generate(pool);

        if (media.size() <= 1 || media.size() > 10)
            throw new TelegRiseRuntimeException("Number of media must be between 2 and 10", node);

        return SendMediaGroup.builder().medias(this.applyCaption(media, parent, pool))
                .chatId(parent.generateChatId(pool))
                .allowSendingWithoutReply(generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .disableNotification(generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent(generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId(generateNullableProperty(parent.getReplyTo(), pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        return inputMedia.generate(pool);
    }

    private List<InputMedia> applyCaption(List<InputMedia> media, Send parent, ResourcePool pool){
        assert media.size() > 1;
        InputMedia first = media.getFirst();
        first.setCaption(parent.getText() != null ? parent.getText().generateText(pool) : null);
        first.setCaptionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of());
        first.setParseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null);

        return media;
    }

    @Override
    public GeneratedValue<String> getFileId() {
        return null;
    }

    @Override
    public GeneratedValue<String> getUrl() {
        return null;
    }

    @Override
    public GeneratedValue<InputFile> getInputFile() { return null; }

    @Override
    public boolean isMediaRequired() { return false; }
}
