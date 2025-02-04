package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "venue")
@Getter @Setter
@NoArgsConstructor
public class Venue extends MediaType{
    @Attribute(name = "latitude", nullable = false)
    private GeneratedValue<Double> latitude;
    @Attribute(name = "longitude", nullable = false)
    private GeneratedValue<Double> longitude;
    @Attribute(name = "title", nullable = false)
    private GeneratedValue<String> title;
    @Attribute(name = "address", nullable = false)
    private GeneratedValue<String> address;
    @Attribute(name = "foursquareId")
    private GeneratedValue<String> foursquareId;
    @Attribute(name = "foursquareType")
    private GeneratedValue<String> foursquareType;
    @Attribute(name = "googlePlaceId")
    private GeneratedValue<String> googlePlaceId;
    @Attribute(name = "googlePlaceType")
    private GeneratedValue<String> googlePlaceType;
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendVenue.builder()
                .latitude(generateNullableProperty(latitude, pool))
                .longitude(generateNullableProperty(longitude, pool))
                .title(generateNullableProperty(title, pool))
                .address(generateNullableProperty(address, pool))
                .foursquareId(generateNullableProperty(foursquareId, pool))
                .foursquareType(generateNullableProperty(foursquareType, pool))
                .googlePlaceId(generateNullableProperty(googlePlaceId, pool))
                .googlePlaceType(generateNullableProperty(googlePlaceType, pool))
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