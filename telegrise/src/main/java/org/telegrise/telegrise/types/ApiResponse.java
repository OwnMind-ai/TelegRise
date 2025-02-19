package org.telegrise.telegrise.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.io.Serializable;

/**
 * A wrapper for API response to executed API method. This class is used in return consumers.
 *
 * @since 0.9
 */
@SuppressWarnings("unused")
@Data @AllArgsConstructor
public class ApiResponse {
    private Object value;

    public boolean hasMessage() {
        return value instanceof Message;
    }

    public Message getMessage(){
        return (Message) value;
    }

    public boolean hasBoolean() {
        return value instanceof Boolean;
    }

    public boolean getBoolean(){
        return (Boolean) value;
    }

    public Serializable getSerializable(){
        return (Serializable) value;
    }
}
