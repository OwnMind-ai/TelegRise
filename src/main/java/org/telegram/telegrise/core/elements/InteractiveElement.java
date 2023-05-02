package org.telegram.telegrise.core.elements;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ResourcePool;

import java.io.Serializable;
import java.util.function.Function;

public interface InteractiveElement <T extends Serializable> extends StorableElement {
    T createIneractiveObject(Function<Update, ResourcePool> resourcePoolFunction);
}
