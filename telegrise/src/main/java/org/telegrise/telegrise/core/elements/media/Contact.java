package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Use this element to send phone contacts.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendcontact">Telegram API: sendContant</a>
 * @since 0.1
 */
@Element(name = "contact")
@Getter @Setter @NoArgsConstructor
public class Contact extends MediaType {
    /**
     * Contact's phone number
     */
    @Attribute(name = "phoneNumber")
    private GeneratedValue<String> phoneNumber;

    /**
     * Contact's first name
     */
    @Attribute(name = "firstName")
    private GeneratedValue<String> firstName;

    /**
     * Contact's last name
     */
    @Attribute(name = "lastName")
    private GeneratedValue<String> lastName;

    /**
     * Additional data about the contact in the form of a {@code vCard}
     */
    @Attribute(name = "vcard")
    private GeneratedValue<String> vcard;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendContact.builder()
                .phoneNumber(generateNullableProperty(phoneNumber, pool))
                .firstName(generateNullableProperty(firstName, pool))
                .lastName(generateNullableProperty(lastName, pool))
                .vCard(generateNullableProperty(vcard, pool))
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
    public boolean isMediaRequired() {
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
