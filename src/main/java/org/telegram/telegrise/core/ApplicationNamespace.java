package org.telegram.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ApplicationNamespace {
    // Key is a simple name of class
    private final Map<String, Class<?>> importedClasses = new HashMap<>();
    private final ClassLoader applicationClassloader;

    @Setter @Getter
    private String updateName = "update";
    @Setter @Getter
    private String controllerName = "controller";
    @Setter @Getter
    private String senderName = "sender";
    @Setter @Getter
    private String memoryName = "memory";

    public ApplicationNamespace(ClassLoader applicationClassloader) {
        this.applicationClassloader = applicationClassloader;
    }

    public void addClass(String name) throws ClassNotFoundException {
        Class<?> loaded = Class.forName(name, true, this.applicationClassloader);
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

    public LocalNamespace emptyLocal(){
        return new LocalNamespace(null, this);
    }

    public Collection<Class<?>> getImportedClasses(){
        return this.importedClasses.values();
    }
}
