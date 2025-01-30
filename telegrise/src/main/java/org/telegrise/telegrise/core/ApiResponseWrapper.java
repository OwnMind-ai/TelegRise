package org.telegrise.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.io.Serializable;

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
