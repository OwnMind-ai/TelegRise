package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
public class Variable {
    private AtomicReference<Object> value;

    public void set(Object value){
        this.value.set(value);
    }

    public Object get(){
        return this.value.get();
    }

    public <T> T get(Class<T> tClass){
        return tClass.cast(value.get());
    }
}
