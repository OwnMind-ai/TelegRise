package org.telegrise.telegrise;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.caching.CachingStrategy;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.UserSession;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.Transition;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.security.Role;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.transition.TransitionController;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.keyboard.KeyboardMarkup;
import org.telegrise.telegrise.transcription.ElementBase;
import org.telegrise.telegrise.types.TextBlock;
import org.telegrise.telegrise.types.UserRole;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class allows access to higher level operations with transcription and attached session, such as
 * transitions, retrieval of named elements and cache manipulations.
 * It can be accessed using {@link org.telegrise.telegrise.annotations.Resource Resource} annotation.
 *
 * @since 0.3
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class TranscriptionManager {
    private final TranscriptionMemory transcriptionMemory;
    private final UserSession.TranscriptionInterrupter interruptor;
    private final SessionMemoryImpl sessionMemory;
    private final TransitionController transitionController;
    private final BiConsumer<BranchingElement, Update> elementExecutor;
    private final BotTranscription transcription;
    private final Function<Update, ResourcePool> resourcePoolProducer;

    @ApiStatus.Internal
    public TranscriptionManager(UserSession.TranscriptionInterrupter interruptor,
                                BiConsumer<BranchingElement, Update> elementExecutor,
                                SessionMemoryImpl sessionMemory,
                                TransitionController transitionController,
                                BotTranscription transcription,
                                Function<Update, ResourcePool> resourcePoolProducer) {
        this.interruptor = interruptor;
        this.sessionMemory = sessionMemory;
        this.transitionController = transitionController;
        this.resourcePoolProducer = resourcePoolProducer;
        this.elementExecutor = elementExecutor;
        this.transcriptionMemory = transcription.getMemory();
        this.transcription = transcription;
    }

    @ApiStatus.Internal
    public TranscriptionManager(BotTranscription transcription, Function<Update, ResourcePool> resourcePoolProducer) {
        this.interruptor = null;
        this.sessionMemory = null;
        this.transitionController = null;
        this.resourcePoolProducer = resourcePoolProducer;
        this.elementExecutor = null;
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

    /**
     * Returns attached session memory
     * @return session memory
     */
    public SessionMemory getSessionMemory(){
        return this.sessionMemory;
    }

    /**
     * Retrieves an instance of a tree controller that is being used at the time of execution, if any.
     * @return controller's instance or null
     */
    public Object getCurrentTreeController(){
        return transitionController.getTreeExecutors().isEmpty() ? null : transitionController.getTreeExecutors().getLast().getControllerInstance();
    }

    /**
     * Retrieves an instance of a tree controller of type {@code T} that is being used at the time of execution, if any.
     * @param tClass class of the controller
     * @return controller's instance or null
     * @param <T> type of the controller
     */
    public <T> T getCurrentTreeController(Class<T> tClass){
        return transitionController.getTreeExecutors().isEmpty() ? null : tClass.cast(transitionController.getTreeExecutors().getLast().getControllerInstance());
    }

    /**
     * Performs a transition with 'back' type, target {@code element} and without execution.
     * @param element named of the tree or branch
     */
    public void transitBack(String element){
        this.transitBack(null, element, false);
    }

    /**
     * Performs a transition with 'back' type,
     * target {@code element} and with execution if {@code execute} is true.
     *
     * @param update update that will be used for element execution
     * @param element named of the tree or branch
     * @param execute if true, the action in a specified element will be executed
     */
    public void transitBack(Update update, String element, boolean execute){
        if (update == null && execute)
            throw new IllegalArgumentException("Targeted element '" + element + "' can not be execute without update");

        checkSessionMemory();

        Transition transition = new Transition();
        transition.setType(Transition.BACK);
        transition.setTarget(GeneratedValue.ofValue(element));
        transition.setExecute(execute);

        // Simulates a naturally closed tree
        if (!this.transitionController.getTreeExecutors().isEmpty())
            this.transitionController.getTreeExecutors().getLast().close();

        this.transitionController.applyTransition(null, transition, this.resourcePoolProducer.apply(update));

        if (execute)
            this.elementExecutor.accept(sessionMemory.getBranchingElements().getLast(), update);
    }

    /**
     * Performs <b>interruption</b> transition to a specified tree without execution.
     * This type of transition duplicates the behavior of <i>natural interruption</i> from one tree to another,
     * by retrieving completely back to root and going to the specified tree.
     *
     * @param treeName tree name
     */
    public void transit(String treeName){
        this.transit(null, treeName, false);
    }

    /**
     * Performs <b>interruption</b> transition to a specified tree with execution if specified.
     * This type of transition duplicates the behavior of <i>natural interruption</i> from one tree to another,
     * by retrieving completely back to root and going to the specified tree.
     *
     * @param update update that will be used for tree execution
     * @param treeName tree name
     * @param execute if true, the action in a specified tree will be executed
     */
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

    /**
     * Retrieves a data object of a named element of type {@code T} if exists.
     * <p>
     * Retrievable elements include: {@link org.telegrise.telegrise.transcription.Branch Branch},
     * {@link org.telegrise.telegrise.transcription.Tree Tree},
     * {@link org.telegrise.telegrise.transcription.Keyboard Keyboard} and
     * {@link org.telegrise.telegrise.transcription.Text Text}.
     *
     * @param name name of the element
     * @param tClass type class
     * @return element's data object or null
     * @param <T> type of the element
     */
    public <T extends ElementBase> T get(String name, Class<T> tClass){
        var result = this.transcriptionMemory.get(this.sessionMemory == null ? null : sessionMemory.getCurrentTree(), name);

        if (result == null)
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");

        if (!ClassUtils.isAssignable(result.getClass(), tClass, true))
            throw new TelegRiseRuntimeException("Element named '" + name + "' is not assignable to '" + tClass.getSimpleName() + "'");

        return tClass.cast(result);
    }

    /**
     * Retrieves {@code <text>} element proxy with specified name.
     * @param name text element name
     * @return text element proxy
     */
    public @NotNull TextBlock getTextBlock(String name){
        return get(name, Text.class).createInteractiveObject(resourcePoolProducer);
    }

    /**
     * Retrieves {@code <keyboard>} element proxy with specified name.
     * @param name text element name
     * @return text element proxy
     */
    public @NotNull KeyboardMarkup getKeyboardMarkup(String name){
        return get(name, Keyboard.class).createInteractiveObject(resourcePoolProducer);
    }

    /**
     * Returns list of user roles that are defined in {@code <roles>} element.
     * @return list of roles
     */
    public List<UserRole> getRoles(){
        return transcriptionMemory.getStandardMemory().values().stream()
                .filter(Role.class::isInstance).map(r -> UserRole.ofRole((Role) r))
                .collect(Collectors.toList());
    }

    //TODO getTextBlock(name, lang)
}
