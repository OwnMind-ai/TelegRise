package org.telegrise.telegrise.core.elements.base;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.ResourcePool;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents an element that can produce a wrapper of itself to be available for public API.
 * @param <T> type of the wrapper
 *
 * @since 0.3
 */
public interface InteractiveElement <T extends Serializable> extends StorableElement {
    T createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction);
}
