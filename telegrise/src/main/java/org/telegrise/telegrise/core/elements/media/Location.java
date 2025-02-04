package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

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
