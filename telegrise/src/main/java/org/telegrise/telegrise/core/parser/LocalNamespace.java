package org.telegrise.telegrise.core.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.senders.BotSender;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor
public class LocalNamespace {
    private Class<?> handlerClass;
    private ApplicationNamespace applicationNamespace;

    public String getResourceInitializationCode(String poolName){
        String handlerClassName = handlerClass != null ? handlerClass.getName() : "Object";

        return String.format("""
                        %s %s = %s.getUpdate();
                        %s %s = %s.getHandler() instanceof %s ? (%s) %s.getHandler() : null;
                        %s %s = %s.getSender();
                        %s %s = %s.getMemory();
                        %s message = %s.getApiResponseWrapper() == null ? null : %s.getApiResponseWrapper().getMessage();
                        %s response = %s.getApiResponseWrapper() == null ? null : %s.getApiResponseWrapper().getSerializable();
                        """,
                Update.class.getName(), applicationNamespace.getUpdateName(), poolName,
                handlerClassName, applicationNamespace.getControllerName(), poolName, handlerClassName, handlerClassName, poolName,
                BotSender.class.getName(), applicationNamespace.getSenderName(), poolName,
                SessionMemory.class.getName(), applicationNamespace.getMemoryName(), poolName,
                Message.class.getName(), poolName, poolName,
                Serializable.class.getName(), poolName, poolName
        );
    }
}
