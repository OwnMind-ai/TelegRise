package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.BranchingElement;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SessionMemory implements Serializable {
    private static final long serialVersionUID = -8011212970107619938L;

    /*
        NOTE: In order to keep compatibly with Redis database, SessionMemory and all their elements should be serializable.
     */
    private final Map<String, Serializable> memory = Collections.synchronizedMap(new HashMap<>());
    @Getter
    private final int transcriptionHashcode;
    @Getter @Setter
    private Branch currentBranch;
    @Getter
    private final Deque<BranchingElement> branchingElements = new ConcurrentLinkedDeque<>();

    public SessionMemory(int transcriptionHashcode) {
        this.transcriptionHashcode = transcriptionHashcode;
    }

    public Map<String, Serializable> getMemoryMap(){
        return memory;
    }

    public void put(String key, Serializable value){
        this.memory.put(key, value);
    }

    public Serializable get(String key){
        return this.memory.get(key);
    }

    public <T extends Serializable> T get(String key, Class<T> tClass){
        return tClass.cast(this.memory.get(key));
    }

    public String addComponent(Serializable value){
        String key = value.getClass().getName();
        this.memory.put(key, value);

        return key;
    }

    public <T extends Serializable> T getComponent(Class<T> tClass){
        return tClass.cast(this.memory.get(tClass.getName()));
    }
}
