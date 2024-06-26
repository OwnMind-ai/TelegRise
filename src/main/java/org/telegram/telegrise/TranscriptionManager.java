package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.transition.TransitionController;
import org.telegram.telegrise.types.KeyboardMarkup;
import org.telegram.telegrise.types.TextBlock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TranscriptionManager {
    private final Map<String, Serializable> objects = new HashMap<>();
    private final UserSession.TranscriptionInterrupter interruptor;
    private final SessionMemoryImpl sessionMemory;
    private final TransitionController transitionController;
    private final BiConsumer<BranchingElement, Update> elementExecutor;
    private final Function<UserIdentifier, TranscriptionManager> transcriptionManagerGetter;
    private BotTranscription transcription;
    private final Function<Update, ResourcePool> resourcePoolProducer;

    public TranscriptionManager(UserSession.TranscriptionInterrupter interruptor, BiConsumer<BranchingElement, Update> elementExecutor, SessionMemoryImpl sessionMemory, TransitionController transitionController, Function<UserIdentifier, TranscriptionManager> transcriptionManagerGetter, Function<Update, ResourcePool> resourcePoolProducer) {
        this.interruptor = interruptor;
        this.sessionMemory = sessionMemory;
        this.transitionController = transitionController;
        this.transcriptionManagerGetter = transcriptionManagerGetter;
        this.resourcePoolProducer = resourcePoolProducer;
        this.elementExecutor = elementExecutor;
    }

    public void load(BotTranscription transcription){
        this.transcription = transcription;
        TranscriptionMemory memory = transcription.getMemory();
        objects.putAll(memory.getElements().entrySet().stream().parallel()
                .filter(e -> e.getValue() instanceof InteractiveElement)
                .map(e -> Map.entry(e.getKey(), ((InteractiveElement<?>) e.getValue()).createInteractiveObject(this.resourcePoolProducer)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    /** Clears cache for specific method reference and returns it previous value.
     * If no cache was stored or method reference was not found, this method will return <code>null</code>.
     *
     * @param instance instance of declaring class
     * @param methodName method name, which should match with <code>Method::getName</code>
     * @return previously cached value
     * @since 0.6
     * @see org.telegram.telegrise.caching.CachingStrategy
     */
    public Object clearCache(Object instance, String methodName){
        return clearCache(instance.getClass(), methodName);
    }

    /** Clears cache for specific method reference and returns it previous value.
     * If no cache was stored or method reference was not found, this method will return <code>null</code>.
     *
     * @param clazz declaring class
     * @param methodName method name, which should match with <code>Method::getName</code>
     * @return previously cached value
     * @since 0.6
     * @see org.telegram.telegrise.caching.CachingStrategy
     */
    public Object clearCache(Class<?> clazz, String methodName){
        return sessionMemory.getCacheMap().entrySet().stream()
                .filter(r -> r.getKey().getMethod().getName().equals(methodName) && r.getKey().getMethod().getDeclaringClass().equals(clazz))
                .map(Map.Entry::getValue).map(c -> {
                    Object res = c.getCachedValue();
                    c.clear();
                    return res;
                }).findFirst().orElse(null);
    }

    public TranscriptionManager getTranscriptionManager(UserIdentifier userIdentifier){
        return this.transcriptionManagerGetter.apply(userIdentifier);
    }

    public SessionMemory getSessionMemory(){
        return this.sessionMemory;
    }

    public <T> T getCurrentTreeController(Class<T> tClass){
        return transitionController.getTreeExecutors().isEmpty() ? null : tClass.cast(transitionController.getTreeExecutors().getLast().getControllerInstance());
    }

    public Tree getCurrentTree(){
        checkSessionMemory();

        BranchingElement last = sessionMemory.getBranchingElements().getLast();
        //TODO return immutable proxy of a tree
        return last instanceof Tree ? (Tree) last : null;
    }

    public Menu getCurrentMenu(){
        checkSessionMemory();

        Iterator<BranchingElement> iterator = sessionMemory.getBranchingElements().descendingIterator();
        while (iterator.hasNext()){
            BranchingElement next = iterator.next();

            if (next instanceof Menu)
                return (Menu) next;  //TODO return immutable proxy of a menu
        }

        return null;
    }

    public void transitPrevious(String element){
        this.transitPrevious(null, element, false);
    }

    public void transitPrevious(Update update, String element, boolean execute){
        if (update == null && execute)
            throw new IllegalArgumentException("Targeted element '" + element + "' can not be execute without update");

        checkSessionMemory();

        Transition transition = new Transition();
        transition.setDirection(Transition.PREVIOUS);
        transition.setTarget(GeneratedValue.ofValue(element));
        transition.setExecute(execute);

        // Simulates a naturally closed tree
        if (!this.transitionController.getTreeExecutors().isEmpty())
            this.transitionController.getTreeExecutors().getLast().close();

        this.transitionController.applyTransition(null, transition, this.resourcePoolProducer.apply(update));

        if (execute)
            this.elementExecutor.accept(sessionMemory.getBranchingElements().getLast(), update);
    }

    public void transit(String treeName){
        this.transit(null, treeName, false);
    }

    public void transit(Update update, String treeName, boolean execute){
        if (update == null && execute)
            throw new IllegalArgumentException("Targeted tree '" + treeName + "' can not be execute without update");

        checkSessionMemory();

        Tree requestedTree = this.transcription.getRootMenu().getTrees().stream()
                .filter(t -> t.getName().equals(treeName)).findFirst().orElse(null);

        if (requestedTree == null) throw new IllegalArgumentException("Tree '" + treeName + "' has not been found at root menu");

        this.interruptor.transit(update, requestedTree, execute);
    }

    private void checkSessionMemory(){
        if (this.sessionMemory == null || this.interruptor == null || transcription == null)
            throw new TelegRiseRuntimeException("Session memory-oriented methods cannot be called from an independent handler");
    }

    private void checkName(String name){
        if (!this.objects.containsKey(name))
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");
    }

    @SuppressWarnings("SameParameterValue")
    private void checkType(String name, Class<?> type){
        if (!type.isInstance(this.objects.get(name)))
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not inherit type '" + type.getSimpleName() + "'");
    }

    public <T extends Serializable> T get(String name, Class<T> tClass){
        checkName(name);
        checkType(name, tClass);

        return tClass.cast(this.objects.get(name));
    }

    public TextBlock getTextBlock(String name){
        checkName(name);
        checkType(name, TextBlock.class);

        return (TextBlock) this.objects.get(name);
    }

    public KeyboardMarkup getKeyboardMarkup(String name){
        checkName(name);
        checkType(name, KeyboardMarkup.class);

        return (KeyboardMarkup) this.objects.get(name);
    }

    //TODO getTextBlock(name, lang)
}
