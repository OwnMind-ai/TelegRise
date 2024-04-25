package org.telegram.telegrise;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public class BotFactory {
    public static LongPollingUpdateConsumer createLongPooling(TelegramSessionsController controller){
        return (LongPollingSingleThreadUpdateConsumer) controller::onUpdateReceived;
    }
}
