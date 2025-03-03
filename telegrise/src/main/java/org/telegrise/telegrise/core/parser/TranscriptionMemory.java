package org.telegrise.telegrise.core.parser;

import lombok.Getter;
import org.slf4j.Logger;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.elements.Branch;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.NamedElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.utils.ReflectionUtils;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TranscriptionMemory implements Serializable {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TranscriptionMemory.class);

    @Getter
    private final Map<String, NodeElement> standardMemory = new HashMap<>();
    private final Map<Tree, Map<String, NodeElement>> treeMemory = new HashMap<>();
    @Getter
    private final List<Consumer<BotTranscription>> tasks = new LinkedList<>();
    @Getter
    private final List<NodeElement> pendingFinalization = new LinkedList<>();
    @Getter
    private final Set<File> linkedFiles = new HashSet<>();

    private boolean readOnly = false;

    public int size() {
        return standardMemory.size();
    }

    public boolean isEmpty() {
        return standardMemory.isEmpty();
    }

    public NodeElement get(String name) {
        return get(null, name);
    }

    public NodeElement get(Tree tree, String name) {
        return Optional.ofNullable(tree)
                .map(treeMemory::get)
                .map(map -> map.get(name))
                .or(() -> Optional.ofNullable(standardMemory.get(name)))
                .orElse(null);
    }

    public <T> T get(String name, Class<T> tClass, List<String> possibleTags){
        return get(null, name, tClass, possibleTags);
    }

    public <T> T get(Tree tree, String name, Class<T> tClass, List<String> possibleTags){
        NodeElement result = get(tree, name);

        if (result == null) throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");

        if (!tClass.isAssignableFrom(result.getClass()))
            throw new TelegRiseRuntimeException(String.format("Element '%s' represents the <%s> tag, required: %s",
                    name, ReflectionUtils.annotation(result, Element.class).name(),
                    possibleTags.stream().map(s -> "<" + s + ">").collect(Collectors.joining(" or "))
            ));

        return tClass.cast(result);
    }

    public void put(Tree currentTree, String name, NodeElement element) {
        if (readOnly) throw new UnsupportedOperationException();

        if ((currentTree != null && treeMemory.getOrDefault(currentTree, Map.of()).containsKey(name)) ||
                (currentTree == null && standardMemory.containsKey(name))) {
            throw new TelegRiseRuntimeException("Name '" + name + "' already exists", element.getElementNode());
        }

        if (element instanceof NamedElement namedElement && namedElement.isGlobal())
            standardMemory.put(name, element);
        else if (currentTree != null && !(element instanceof Tree))
            treeMemory.computeIfAbsent(currentTree, t -> new HashMap<>()).put(name, element);
        else {
            standardMemory.put(name, element);

            if (element instanceof Branch)
                logger.warn("Branch '{}' is not associated with any tree", name);
        }
    }

    public void set(Tree currentTree, String name, NodeElement element){
        if (readOnly) throw new UnsupportedOperationException();
        Optional.ofNullable(currentTree).map(treeMemory::get)
                .ifPresentOrElse(
                        map -> map.put(name, element),
                        () -> standardMemory.put(name, element)
                );
    }

    public boolean containsKey(String key){
        return containsKey(null, key);
    }

    public boolean containsKey(Tree currentTree, String key){
        return Optional.ofNullable(currentTree).map(treeMemory::get)
                .map(map -> map.containsKey(key))
                .map(v -> v ? true : null) // so that the code below executes only if the key wasn't found in treeMemory
                .orElseGet(() -> standardMemory.containsKey(key));
    }

    public void setReadOnly(){
        this.readOnly = true;
    }
}
