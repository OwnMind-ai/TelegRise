package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@Data @AllArgsConstructor @NoArgsConstructor
public final class ResourcePool {
    private Update update;
    private Object handler;

    //FIXME Use map instead
    private String updateName = "update";
    private String handlerName = "handler";  //TODO try to make it "this" by default

    public ResourcePool(Update update, Object handler){
        this.update = update;
        this.handler = handler;
    }

    public String getResourceInitializationCode(String poolName){
        return String.format("Update %s = %s.getUpdate();\n%s %s = (%s) %s.getHandler();\n",
                updateName, poolName, handler.getClass().getName(), handlerName, handler.getClass().getName(), poolName);

    }
}