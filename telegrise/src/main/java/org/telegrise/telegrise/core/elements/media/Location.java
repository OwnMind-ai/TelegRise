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

/**
 * Use this element to send a point on the map.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendlocation">Telegram API: sendLocation</a>
 * @since 0.1
 */
@Element(name = "location")
@Getter @Setter @NoArgsConstructor
public class Location extends MediaType{
    /**
     * Latitude of the location
     */
    @Attribute(name = "latitude", nullable = false)
    private GeneratedValue<Double> latitude;
    /**
     * Longitude of the location
     */
    @Attribute(name = "longitude", nullable = false)
    private GeneratedValue<Double> longitude;
    /**
     * The radius of uncertainty for the location, measured in meters
     */
    @Attribute(name = "horizontalAccuracy")
    private GeneratedValue<Double> horizontalAccuracy;
    /**
     * Period in seconds during which the location will be updated
     */
    @Attribute(name = "livePeriod")
    private GeneratedValue<Integer> livePeriod;
    /**
     * For live locations, a direction in which the user is moving, in degrees
     */
    @Attribute(name = "heading")
    private GeneratedValue<Integer> heading;
    /**
     * For live locations, a maximum distance for proximity alerts about approaching another chat member, in meters
     */
    @Attribute(name = "proximityAlertRadius")
    private GeneratedValue<Integer> proximityAlertRadius;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
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
