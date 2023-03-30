package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface PrimaryHandler {
    boolean canHandle(Update update);
    void handle(Update update);
}
