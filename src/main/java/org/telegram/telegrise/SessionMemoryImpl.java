package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.transition.JumpPoint;
import org.telegram.telegrise.types.UserRole;

import java.io.Serializable;
import java.util.*;
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
    private final UserIdentifier userIdentifier;
    @Getter
    private final AtomicReference<Branch> currentBranch = new AtomicReference<>();
    @Getter
    private final Deque<BranchingElement> branchingElements = new ConcurrentLinkedDeque<>();
    @Getter
    private final Deque<JumpPoint> jumpPoints = new ConcurrentLinkedDeque<>();

    @Getter @Setter
    private UserRole userRole;
    @Getter @Setter
    private Message lastSentMessage;

    public SessionMemoryImpl(int transcriptionHashcode, UserIdentifier userIdentifier) {
        this.transcriptionHashcode = transcriptionHashcode;
        this.userIdentifier = userIdentifier;
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
    public boolean containsKey(String key) {
        return this.memory.containsKey(key);
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
        return !this.branchingElements.isEmpty() && clazz.isInstance(this.branchingElements.getLast());
    }

    public <T> T getFromStack(Class<T> tClass){
        assert tClass.isInstance(this.branchingElements.getLast());

        return tClass.cast(this.branchingElements.getLast());
    }

    public String[] getLastChatTypes(){
        for (Iterator<BranchingElement> it = this.branchingElements.descendingIterator(); it.hasNext(); ) {
            BranchingElement element = it.next();

            if (element.getChatTypes() != null)
                return element.getChatTypes();
        }

        return new String[]{};
    }

    public void updateJumpPoints(){
        this.jumpPoints.forEach(p -> {
            if (!this.branchingElements.contains(p.getTo())) this.jumpPoints.remove(p);
        });
    }
}
