package org.telegrise.telegrise.core.parser;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.builtin.DefaultController;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.senders.BotSender;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ApplicationNamespace {
    // Key is a simple name of class
    private final Map<String, Class<?>> importedClasses = new HashMap<>();
    private final ClassLoader applicationClassloader;
    @Getter
    private final String applicationPackageName;

    @Setter @Getter
    private String updateName = "update";
    @Setter @Getter
    private String controllerName = "controller";
    @Setter @Getter
    private String senderName = "sender";
    @Setter @Getter
    private String memoryName = "memory";

    public ApplicationNamespace(ClassLoader applicationClassloader, String applicationPackageName) {
        this.applicationClassloader = applicationClassloader;
        this.applicationPackageName = applicationPackageName;
        importedClasses.put(DefaultController.class.getSimpleName(), DefaultController.class);
    }

    public void addClass(String name) throws ClassNotFoundException {
        addClass(Class.forName(name, true, this.applicationClassloader));
    }

    public void addClass(Class<?> loaded){
        var existing = importedClasses.get(loaded.getSimpleName());
        if (existing != null && !existing.equals(loaded))
            throw new TelegRiseRuntimeException("Conflict of imported classes: '%s' and '%s'"
                    .formatted(existing.getName(), loaded.getName()));

        importedClasses.put(loaded.getSimpleName(), loaded);
    }

    public @NotNull Class<?> getClass(String name){
        String className = name;
        if (className.contains("."))
            className = className.substring(className.lastIndexOf('.'));

        if (this.importedClasses.containsKey(className))
            return this.importedClasses.get(className);
        else {
            try {
                return Class.forName(name, true, this.applicationClassloader);
            } catch (ClassNotFoundException e) {
                throw new TelegRiseRuntimeException("Unable to find class '" + name + "'");
            }
        }
    }

    public String getNameOfGlobal(Class<?> clazz){
        if (clazz.equals(Update.class)) return updateName;
        if (BotSender.class.isAssignableFrom(clazz)) return senderName;
        if (SessionMemory.class.isAssignableFrom(clazz)) return memoryName;
        throw new IllegalArgumentException(clazz.getName());
    }

    public LocalNamespace emptyLocal(){
        return new LocalNamespace(null, this);
    }

    public Collection<Class<?>> getImportedClasses(){
        return this.importedClasses.values();
    }
}
