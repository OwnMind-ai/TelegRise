package org.telegram.telegrise;

import lombok.Getter;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.BranchingElement;

import java.io.Serializable;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SessionMemoryImpl implements SessionMemory {
    private static final long serialVersionUID = -8011212970107619938L;

    /*
        NOTE: In order to keep compatibly with Redis database, SessionMemory and all their elements should be serializable.
     */
    private final Map<String, Serializable> memory = Collections.synchronizedMap(new HashMap<>());
    @Getter
    private final int transcriptionHashcode;
    @Getter
    private final AtomicReference<Branch> currentBranch = new AtomicReference<>();
    @Getter
    private final Deque<BranchingElement> branchingElements = new ConcurrentLinkedDeque<>();

    public SessionMemoryImpl(int transcriptionHashcode) {
        this.transcriptionHashcode = transcriptionHashcode;
    }

    @Override
    public Map<String, Serializable> getMemoryMap(){
        return memory;
    }

    @Override
    public void put(String key, Serializable value){
        this.memory.put(key, value);
    }

    @Override
    public Serializable get(String key){
        return this.memory.get(key);
    }

    @Override
    public <T extends Serializable> T get(String key, Class<T> tClass){
        return tClass.cast(this.memory.get(key));
    }

    @Override
    public String addComponent(Serializable value){
        String key = value.getClass().getName();
        this.memory.put(key, value);

        return key;
    }

    @Override
    public <T extends Serializable> T getComponent(Class<T> tClass){
        return tClass.cast(this.memory.get(tClass.getName()));
    }

    public boolean isOnStack(Class<?> clazz){
        return !this.memory.isEmpty() && clazz.isInstance(this.branchingElements.getLast());
    }

    public <T> T getFromStack(Class<T> tClass){
        assert tClass.isInstance(this.branchingElements.getLast());

        return tClass.cast(this.branchingElements.getLast());
    }
}
