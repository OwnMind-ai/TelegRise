package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.BotSender;
import org.telegram.telegrise.SessionMemory;

@Data @NoArgsConstructor @AllArgsConstructor
public class LocalNamespace {
    private Class<?> handlerClass;
    private ApplicationNamespace applicationNamespace;

    public String getResourceInitializationCode(String poolName){
        String handlerClassName = handlerClass != null ? handlerClass.getName() : "Object";

        return String.format("%s %s = %s.getUpdate();\n" +
                        "%s %s = %s.getHandler() instanceof %s ? (%s) %s.getHandler() : null;\n" +
                        "%s %s = %s.getSender();\n" +
                        "%s %s = %s.getMemory();\n",
                Update.class.getName(), applicationNamespace.getUpdateName(), poolName,
                handlerClassName, applicationNamespace.getControllerName(), poolName, handlerClassName, handlerClassName, poolName,
                BotSender.class.getName(), applicationNamespace.getSenderName(), poolName,
                SessionMemory.class.getName(), applicationNamespace.getMemoryName(), poolName
        );
    }
}
