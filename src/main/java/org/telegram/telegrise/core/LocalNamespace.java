package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@Data @NoArgsConstructor @AllArgsConstructor
public class LocalNamespace {
    private Class<?> handlerClass;
    private ApplicationNamespace applicationNamespace;

    public String getResourceInitializationCode(String poolName){
        String handlerClassName = handlerClass != null ? handlerClass.getName() : "Object";

        return String.format("%s %s = %s.getUpdate();\n%s %s = (%s) %s.getHandler();\n",
                Update.class.getName(), applicationNamespace.getUpdateName(), poolName,
                handlerClassName, applicationNamespace.getHandlerName(),
                handlerClassName, poolName);
    }
}
