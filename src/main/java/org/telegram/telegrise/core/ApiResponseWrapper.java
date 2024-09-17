package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

import org.telegram.telegrambots.meta.api.objects.message.Message;

@Data @AllArgsConstructor
public class ApiResponseWrapper {
    private Object value;

    public Message getMessage(){
        return value instanceof Message m ? m : null;
    }

    public Serializable getSerializable(){
        return (Serializable) value;
    }
}
