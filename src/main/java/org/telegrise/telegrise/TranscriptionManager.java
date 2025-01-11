package org.telegrise.telegrise;

import org.apache.commons.lang3.ClassUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.caching.CachingStrategy;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.BranchingElement;
import org.telegrise.telegrise.core.elements.Transition;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.transition.TransitionController;
import org.telegrise.telegrise.types.KeyboardMarkup;
import org.telegrise.telegrise.types.TextBlock;

import java.io.Serializable;
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
    private final Function<SessionIdentifier, TranscriptionManager> transcriptionManagerGetter;
    private final BotTranscription transcription;
    private final Function<Update, ResourcePool> resourcePoolProducer;

    public TranscriptionManager(UserSession.TranscriptionInterrupter interruptor,
                                BiConsumer<BranchingElement, Update> elementExecutor,
                                SessionMemoryImpl sessionMemory,
                                TransitionController transitionController,
                                BotTranscription transcription,
                                Function<SessionIdentifier, TranscriptionManager> transcriptionManagerGetter,
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

    /** Clears cache for specific method reference and returns its previous value.
     * If no cache was stored or method reference was not found, this method will return <code>null</code>.
     *
     * @param instance instance of declaring a class
     * @param methodName method name, which should match with <code>Method::getName</code>
     * @return previously cached value
     * @since 0.6
     * @see CachingStrategy
     */
    public Object clearCache(Object instance, String methodName){
        return clearCache(instance.getClass(), methodName);
    }

    /** Clears cache for specific method reference and returns its previous value.
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

    public TranscriptionManager getTranscriptionManager(SessionIdentifier sessionIdentifier){
        return this.transcriptionManagerGetter.apply(sessionIdentifier);
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

    public void transitBack(String element){
        this.transitBack(null, element, false);
    }

    public void transitBack(Update update, String element, boolean execute){
        if (update == null && execute)
            throw new IllegalArgumentException("Targeted element '" + element + "' can not be execute without update");

        checkSessionMemory();

        Transition transition = new Transition();
        transition.setDirection(Transition.BACK);
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

        Tree requestedTree = this.transcription.getRoot().getTrees().stream()
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
