package org.telegrise.telegrise.core.elements.base;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.ResourcePool;

import java.io.Serializable;
import java.util.function.Function;

public interface InteractiveElement <T extends Serializable> extends StorableElement {
    T createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction);
}
