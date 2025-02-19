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

/**
 * Use this element to send information about a venue.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendvanue">Telegram API: sendVenue</a>
 * @since 0.1
 */
@Element(name = "venue")
@Getter @Setter
@NoArgsConstructor
public class Venue extends MediaType{
    /**
     * Latitude of the venue
     */
    @Attribute(name = "latitude", nullable = false)
    private GeneratedValue<Double> latitude;
    /**
     * Longitude of the venue
     */
    @Attribute(name = "longitude", nullable = false)
    private GeneratedValue<Double> longitude;
    /**
     * Name of the venue
     */
    @Attribute(name = "title", nullable = false)
    private GeneratedValue<String> title;
    /**
     * Address of the venue
     */
    @Attribute(name = "address", nullable = false)
    private GeneratedValue<String> address;
    /**
     * Foursquare identifier of the venue
     */
    @Attribute(name = "foursquareId")
    private GeneratedValue<String> foursquareId;
    /**
     * Foursquare type of the venue, if known
     */
    @Attribute(name = "foursquareType")
    private GeneratedValue<String> foursquareType;
    /**
     * Google Places identifier of the venue
     */
    @Attribute(name = "googlePlaceId")
    private GeneratedValue<String> googlePlaceId;
    /**
     * Google Places type of the venue
     */
    @Attribute(name = "googlePlaceType")
    private GeneratedValue<String> googlePlaceType;
    /**
     * Determines if this element must be executed (if returns {@code true})
     */
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
                .replyMarkup(parent.createKeyboard(pool))
                .replyParameters(parent.createReplyParameters(pool))
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