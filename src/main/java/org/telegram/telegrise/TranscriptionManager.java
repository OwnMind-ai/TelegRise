package org.telegram.telegrise;

import org.apache.commons.lang3.ClassUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.caching.CachingStrategy;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.transition.TransitionController;
import org.telegram.telegrise.types.KeyboardMarkup;
import org.telegram.telegrise.types.TextBlock;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TranscriptionManager {
    private final TranscriptionMemory transcriptionMemory;
    private final UserSession.TranscriptionInterrupter interruptor;
    private final SessionMemoryImpl sessionMemory;
    private final TransitionController transitionController;
    private final BiConsumer<BranchingElement, Update> elementExecutor;
    private final Function<UserIdentifier, TranscriptionManager> transcriptionManagerGetter;
    private final BotTranscription transcription;
    private final Function<Update, ResourcePool> resourcePoolProducer;

    public TranscriptionManager(UserSession.TranscriptionInterrupter interruptor,
                                BiConsumer<BranchingElement, Update> elementExecutor,
                                SessionMemoryImpl sessionMemory,
                                TransitionController transitionController,
                                BotTranscription transcription,
                                Function<UserIdentifier, TranscriptionManager> transcriptionManagerGetter,
                                Function<Update, ResourcePool> resourcePoolProducer) {
        this.interruptor = interruptor;
        this.sessionMemory = sessionMemory;
        this.transitionController = transitionController;
        this.transcriptionManagerGetter = transcriptionManagerGetter;
        this.resourcePoolProducer = resourcePoolProducer;
        this.elementExecutor = elementExecutor;
        this.transcriptionMemory = transcription.getMemory();
        this.transcription = transcription;
    }

    /** Clears cache for specific method reference and returns it previous value.
     * If no cache was stored or method reference was not found, this method will return <code>null</code>.
     *
     * @param instance instance of declaring class
     * @param methodName method name, which should match with <code>Method::getName</code>
     * @return previously cached value
     * @since 0.6
     * @see CachingStrategy
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
     * @see CachingStrategy
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
            throw new TelegRiseRuntimeException("Memory-oriented methods cannot be called from an independent handler");
    }

    public <T extends Serializable> T get(String name, Class<T> tClass){
        var result = this.transcriptionMemory.get(this.sessionMemory == null ? null : getCurrentTree(), name);

        if (result == null)
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");

        if (!ClassUtils.isAssignable(result.getClass(), tClass, true))
            throw new TelegRiseRuntimeException("Element named '" + name + "' is not assignable to '" + tClass.getSimpleName() + "'");

        return tClass.cast(result);
    }

    public TextBlock getTextBlock(String name){
        return get(name, Text.class).createInteractiveObject(resourcePoolProducer);
    }

    public KeyboardMarkup getKeyboardMarkup(String name){
        return get(name, Keyboard.class).createInteractiveObject(resourcePoolProducer);
    }

    //TODO getTextBlock(name, lang)
}
