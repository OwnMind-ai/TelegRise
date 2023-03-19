package org.telegram.telegrise.core;

import lombok.Getter;
import lombok.Setter;

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
    private String handlerName = "handler";   //TODO try to make it "this" by default

    public ApplicationNamespace(ClassLoader applicationClassloader) {
        this.applicationClassloader = applicationClassloader;
    }

    public void addClass(String name) throws ClassNotFoundException {
        Class<?> loaded = Class.forName(name, true, this.applicationClassloader);
        importedClasses.put(loaded.getSimpleName(), loaded);
    }

    public Class<?> getClass(String name){
        if (name.contains("."))
            name = name.substring(name.lastIndexOf('.'));

        return this.importedClasses.get(name);
    }

    public LocalNamespace localNamespaceForClass(String classname){
        return new LocalNamespace(this.getClass(classname), this);
    }

    public LocalNamespace emptyLocal(){
        return new LocalNamespace(null, this);
    }

    public Collection<Class<?>> getImportedClasses(){
        return this.importedClasses.values();
    }
}
