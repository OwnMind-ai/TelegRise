package org.telegram.telegrise.core.elements.media;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "location")
@Getter @Setter @NoArgsConstructor
public class Location extends MediaType{
    @Attribute(name = "latitude", nullable = false)
    private GeneratedValue<Double> latitude;
    @Attribute(name = "longitude", nullable = false)
    private GeneratedValue<Double> longitude;
    @Attribute(name = "horizontalAccuracy")
    private GeneratedValue<Double> horizontalAccuracy;
    @Attribute(name = "livePeriod")
    private GeneratedValue<Integer> livePeriod;
    @Attribute(name = "heading")
    private GeneratedValue<Integer> heading;
    @Attribute(name = "proximityAlertRadius")
    private GeneratedValue<Integer> proximityAlertRadius;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendLocation.builder()
                .latitude(generateNullableProperty(latitude, pool))
                .longitude(generateNullableProperty(longitude, pool))
                .horizontalAccuracy(generateNullableProperty(horizontalAccuracy, pool))
                .livePeriod(generateNullableProperty(livePeriod, pool))
                .heading(generateNullableProperty(heading, pool))
                .proximityAlertRadius(generateNullableProperty(proximityAlertRadius, pool))
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

    @Override
    public boolean isMediaRequired() {
        return false;
    }
}
