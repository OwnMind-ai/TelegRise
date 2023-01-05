package org.telegram.telegrise.core;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Variables {
    private final Map<String, Variable> variables;

    public Variables(Map<String, Variable> variables) {
        this.variables = variables;
    }

    public <T> T get(String name, Class<T> tClass){
        return this.variables.get(name).get(tClass);
    }

    public void set(String name, Object value){
        this.variables.get(name).set(value);
    }

    public void add(String name, Object value){
        this.variables.put(name, new Variable(new AtomicReference<>(value)));
    }
}
