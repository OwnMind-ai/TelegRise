package org.telegram.telegrise.core.elements.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "dice")
@Data @NoArgsConstructor
public class Dice implements MediaType{
    @Attribute(name = "emoji")
    private GeneratedValue<String> emoji;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendDice.builder()
                .emoji(generateNullableProperty(emoji, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .allowSendingWithoutReply( generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        return null;
    }

    @Override
    public boolean isGroupable() {
        return false;
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
    public GeneratedValue<InputFile> getInputFile() {
        return null;
    }
}
