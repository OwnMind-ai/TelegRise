package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.Variables;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Invocable {
    void run(Update update, Variables variables) throws Exception;
}
