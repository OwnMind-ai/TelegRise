package org.telegrise.telegrise.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.StaticReferenceHolder;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.Objects;

@StaticReferenceHolder
public class ReferenceHolder {
    @Reference
    public String firstName(Update update){
        return Objects.requireNonNull(MessageUtils.getFrom(update)).getFirstName();
    }
}
