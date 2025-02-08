package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.caching.CachingStrategy;
import org.telegrise.telegrise.core.caching.MethodReferenceCache;
import org.telegrise.telegrise.core.elements.Branch;
import org.telegrise.telegrise.core.elements.Root;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.expressions.references.MethodReference;
import org.telegrise.telegrise.core.transition.JumpPoint;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.keyboard.KeyboardState;
import org.telegrise.telegrise.types.UserRole;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SessionMemoryImpl implements SessionMemory {
    private final Map<String, Object> memory = new ConcurrentHashMap<>();
    @Getter
    private final int transcriptionHashcode;
    @Getter
    private final SessionIdentifier sessionIdentifier;
    private final AtomicReference<Branch> currentBranch = new AtomicReference<>();

    @Getter
    private final Deque<BranchingElement> branchingElements = new ConcurrentLinkedDeque<>();
    @Getter
    private final Deque<JumpPoint> jumpPoints = new ConcurrentLinkedDeque<>();
    @Getter
    private final String botUsername;
    private transient final Map<String, UserRole> roleMap;

    @Getter
    private final transient Map<MethodReference, MethodReferenceCache> cacheMap = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> registryMap = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, KeyboardState> keyboardStates = new ConcurrentHashMap<>();

    @Getter
    private UserRole userRole;
    @Getter @Setter
    private String languageCode;
    @Getter @Setter
    private Message lastSentMessage;

    public SessionMemoryImpl(int transcriptionHashcode, SessionIdentifier sessionIdentifier, String botUsername, Map<String, UserRole> roleMap) {
        this.transcriptionHashcode = transcriptionHashcode;
        this.sessionIdentifier = sessionIdentifier;
        this.botUsername = botUsername;
        this.roleMap = roleMap;
    }

    public Branch getCurrentBranch(){
        return currentBranch.get();
    }

    @Override
    public @Nullable Tree getCurrentTree(){
        return isOnStack(Tree.class) ? getFromStack(Tree.class) : null;
    }

    public void setCurrentBranch(Branch branch){
        currentBranch.set(branch);
    }

    @Override
    public Map<String, Object> getMemoryMap(){
        return memory;
    }

    @Override
    public void put(String key, Object value){
        this.memory.put(key, value);
    }

    @Override
    public boolean containsKey(String key) {
        return this.memory.containsKey(key);
    }

    @Override
    public Object get(String key){
        return this.memory.get(key);
    }

    @Override
    public Object remove(String key) {
        return this.memory.remove(key);
    }

    @Override
    public <T> T get(String key, Class<T> tClass){
        return tClass.cast(this.memory.get(key));
    }

    @Override
    public String addComponent(Object value){
        String key = value.getClass().getName();
        this.memory.put(key, value);

        return key;
    }

    @Override
    public <T> T getComponent(Class<T> tClass){
        return tClass.cast(this.memory.get(tClass.getName()));
    }

    @Override
    public <T> T removeComponent(Class<T> tClass) {
        return tClass.cast(this.memory.remove(tClass.getName()));
    }

    @Override
    public <T> boolean containsComponent(Class<T> tClass) {
        return this.memory.containsKey(tClass.getName());
    }

    @Override
    public void setUserRole(String roleName) {
       this.userRole = roleMap.get(roleName);
       if (userRole == null)
           throw new TelegRiseRuntimeException("Unrecognized role '%s' for session %s".formatted(roleName, sessionIdentifier));
    }

    @Override
    public long getUserId() {
        return this.sessionIdentifier.getUserId();
    }

    @Override
    public long getChatId() {
        return sessionIdentifier.getChatId();
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
            if (!this.branchingElements.contains(p.to())) this.jumpPoints.remove(p);
        });
    }

    public MethodReferenceCache getMethodReferenceCache(MethodReference reference){
        MethodReferenceCache res = this.cacheMap.get(reference);
        if (res == null) {
            CachingStrategy strategy = reference.getMethod().getAnnotation(Reference.class).caching();
            if (strategy != CachingStrategy.NONE) {
                res = new MethodReferenceCache(strategy);
                this.cacheMap.put(reference, res);
            }
        }

        return res;
    }

    public boolean isOnRoot() {
        return branchingElements.size() == 1 && branchingElements.getFirst() instanceof Root;
    }

    @Override
    public List<Message> getRegistry(String name) {
        return this.registryMap.computeIfAbsent(name, k -> new ArrayList<>());
    }

    @Override
    public List<Message> clearRegistry(String name) {
        var result = new ArrayList<>(getRegistry(name));
        this.registryMap.get(name).clear();

        return result;
    }

    @Override
    public void putToRegistry(String name, Message message) {
        this.registryMap.computeIfAbsent(name, k -> new ArrayList<>()).add(message);
    }

    @Override
    public KeyboardState getKeyboardState(String name, org.telegrise.telegrise.transcription.Tree parentTree) {
        return this.keyboardStates.get(parentTree.getName() + ":" + name);
    }

    public void putKeyboardState(String name, Tree parentTree, KeyboardState state) {
        this.keyboardStates.put(parentTree.getName() + ":" + name, state);
    }
}
