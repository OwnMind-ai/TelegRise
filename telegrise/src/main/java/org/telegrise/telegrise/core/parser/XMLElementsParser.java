package org.telegrise.telegrise.core.parser;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegrise.telegrise.core.elements.Root;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.base.StorableElement;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.expressions.Syntax;
import org.telegrise.telegrise.core.utils.ReflectionUtils;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class XMLElementsParser {
    private static final String ELEMENTS_PACKAGE = "org.telegrise.telegrise.core.elements";

    private static Set<Class<? extends NodeElement>> loadClasses(){
        return new Reflections(ELEMENTS_PACKAGE).getSubTypesOf(NodeElement.class);
    }

    private final Map<String, Class<? extends NodeElement>> elements = new HashMap<>();
    @Setter @Getter
    private LocalNamespace namespace;
    @Getter
    private final TranscriptionMemory transcriptionMemory = new TranscriptionMemory();
    @Getter
    private final File rootDirectory;
    @Setter
    private Tree currentTree;

    public XMLElementsParser(LocalNamespace namespace, File rootDirectory){
        assert rootDirectory == null || rootDirectory.isDirectory();

        this.namespace = namespace;
        this.rootDirectory = rootDirectory;
    }

    public void load(){
        XMLElementsParser.loadClasses().stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
            assert clazz.isAnnotationPresent(Element.class) :
                    "Element class " + clazz.getCanonicalName() + " is not Element annotation present";

            Element element = clazz.getAnnotation(Element.class);

            assert !this.elements.containsKey(element.name()) : String.format("Class %s and %s has the same name %s",
                    clazz.getCanonicalName(), this.elements.get(element.name()).getCanonicalName(), element.name());

            this.elements.put(element.name(), clazz);
        });
    }

    public NodeElement parse(@NotNull Node node) throws Exception {
        return parse(node, null);
    }

    public NodeElement parse(@NotNull Node node, NodeElement parent) throws Exception {
        Class<? extends NodeElement> element = this.elements.get(node.getNodeName());
        NodeElement instance = element.getConstructor().newInstance();
        instance.setElementNode(node);

        if (instance instanceof Tree tree)
            currentTree = tree;
        else if (instance instanceof Root)
            currentTree = null;

        instance.setParentTree(currentTree);
        instance.setParent(parent);

        final Map<Class<?>, Object> resourcesMap = Map.of(
                Node.class, node,
                LocalNamespace.class, this.namespace,
                TranscriptionMemory.class, this.transcriptionMemory,
                XMLElementsParser.class, this
        );

        Set<String> expected = new HashSet<>();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
            expected.add(attributes.item(i).getNodeName());

        Stream.concat(
                Arrays.stream(element.getDeclaredFields()).filter(m -> m.isAnnotationPresent(Attribute.class))
                        .map(f -> Map.<Attribute, Object>entry(f.getAnnotation(Attribute.class), f)),
                Arrays.stream(element.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Attribute.class))
                        .map(m -> Map.<Attribute, Object>entry(m.getAnnotation(Attribute.class), m))
            )
            .sorted(Comparator.<Map.Entry<Attribute, Object>>comparingDouble(e -> e.getKey().priority()).reversed())
            .forEach(entry -> {
                try {
                    if(entry.getValue() instanceof Method)
                        this.parseMethod(instance, resourcesMap, (Method) entry.getValue());
                    else if (entry.getValue() instanceof Field) {
                        this.parseField((Field) entry.getValue(), node, instance);
                    }

                    expected.remove(entry.getKey().name());  // Records attribute as processed
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    throw new TelegRiseInternalException(e);
                } catch (InvocationTargetException e) {
                    throw new TelegRiseInternalException(e.getTargetException());
                }
            });

        if (!expected.isEmpty())
            throw new TranscriptionParsingException("Unrecognized attributes: " + String.join(", ", expected), node);

        if (element.getAnnotation(Element.class).checkInner()){
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node item = node.getChildNodes().item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE)
                    expected.add(item.getNodeName());
            }
        }

        LocalNamespace newNamespace = instance.createNamespace(this.namespace.getApplicationNamespace());
        if (newNamespace != null)
            this.namespace = newNamespace;

        AtomicBoolean hasEmbedded = new AtomicBoolean(false);
        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(InnerElement.class))
                .sorted(Comparator.<Field>comparingDouble(f -> f.getAnnotation(InnerElement.class).priority()).reversed())
                .forEach(f -> {
                    Tree oldTree = currentTree;
                    if (this.parseInnerElement(node, f, instance, expected))
                        hasEmbedded.set(true);

                    currentTree = oldTree;
                });

        if (!expected.isEmpty() && !hasEmbedded.get())
            throw new TranscriptionParsingException("Unrecognized elements: " + String.join(", ", expected), node);

        finishElement(instance);

        return instance;
    }

    private void finishElement(NodeElement instance) {
        // ORDER MATTERS:
        if(instance.getClass().getAnnotation(Element.class).finishAfterParsing())
            this.transcriptionMemory.getPendingFinalization().add(instance);
        else {
            instance.validate(transcriptionMemory, namespace.getApplicationNamespace());
            instance.load(transcriptionMemory);
        }

        //NOTE: Class Keyboard requires method store to run before the load (if finishAfterParsing)
        if (instance instanceof StorableElement)
            ((StorableElement) instance).store(transcriptionMemory);
    }

    private void parseMethod(NodeElement instance, Map<Class<?>, Object> resourcesMap, Method method) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        Object[] parameters = Arrays.stream(method.getParameterTypes()).map(resourcesMap::get).toArray();

        Object result = method.invoke(instance, parameters);

        if (result instanceof LocalNamespace)
            this.namespace = (LocalNamespace) result;
    }

    private boolean parseInnerElement(Node node, Field field, NodeElement instance, Set<String> expected){
        NodeList nodeList = node.getChildNodes();
        InnerElement fieldData = field.getAnnotation(InnerElement.class);
        Class<?> actualType = ReflectionUtils.getRawGenericType(field);

        Element innerElementData = actualType.getAnnotation(Element.class);

        HashSet<String> nodeNames = new HashSet<>();
        LinkedList<Node> fieldNodes = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            // Node name equals Element.name() or Element type is inherited
            if ((innerElementData != null && nodeList.item(i).getNodeName().equals(innerElementData.name()))
                    || (elements.containsKey(nodeList.item(i).getNodeName())
                    && actualType.isAssignableFrom(elements.get(nodeList.item(i).getNodeName())))) {
                fieldNodes.add(nodeList.item(i));
                expected.remove(nodeList.item(i).getNodeName());
            }

            nodeNames.add(nodeList.item(i).getNodeName());
        }

        if (fieldNodes.isEmpty()){
            if (EmbeddableElement.class.isAssignableFrom(actualType)) {
                if (nodeList.getLength() == 0) return false;
                //FIXME optimize (use #text node name)
                if (Arrays.stream(field.getDeclaringClass().getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(InnerElement.class))
                        .flatMap(f -> {
                            Class<?> actual = ReflectionUtils.getRawGenericType(f);
                            if (actual.isAnnotationPresent(Element.class))
                                return Stream.of(actual);
                            else
                                return elements.values().stream().filter(actual::isAssignableFrom);
                        })
                        .map(c -> c.getAnnotation(Element.class).name())
                        .anyMatch(nodeNames::contains)
                ) {
                    if (fieldData.nullable()) return false;
                    else {
                        throw new TranscriptionParsingException("Embedded element '" + Objects.requireNonNull(innerElementData).name() + "' is not allowed here, use <" + innerElementData.name() + "> instead", node);
                    }
                }

                try {
                    EmbeddableElement embeddableElement = (EmbeddableElement) actualType.getConstructor().newInstance();
                    embeddableElement.parse(node, this.namespace);

                    NodeElement object = (NodeElement) embeddableElement;

                    finishElement(object);

                    PropertyUtils.setSimpleProperty(instance, field.getName(), object);
                    return true;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new TelegRiseInternalException(e);
                }
            }
            else if (fieldData.nullable()) return false;
            else throw new TranscriptionParsingException("Field \"" + (innerElementData != null ? innerElementData.name() : field.getName()) + "\" can't be null", node);
        }

        try {
            if (List.class.isAssignableFrom(field.getType())) {
                PropertyUtils.setSimpleProperty(instance, field.getName(), fieldNodes.stream()
                        .map(n -> {
                            try { return parse(n, instance); } catch (Exception e) { throw new TelegRiseInternalException(e); }
                        }).toList()
                );
            } else {
                if (fieldNodes.size() == 1)
                    PropertyUtils.setSimpleProperty(instance, field.getName(), this.parse(fieldNodes.getFirst(), instance));
                else
                    throw new TranscriptionParsingException(
                            "Field \"" + (innerElementData != null ? innerElementData.name() : field.getName()) + "\" has more than one definition", node);
            }
        } catch (Exception e) {
            throw new TelegRiseInternalException(e);
        }

        return false;
    }

    private void parseField(Field field, Node node, NodeElement instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        field.setAccessible(true);
        Attribute elementData = field.getAnnotation(Attribute.class);

        if(elementData.isTextContext())
            field.set(instance, node.getTextContent());
        else if (!elementData.name().isEmpty()) {
            this.setField(node, elementData, instance, field);
        }
    }

    private void setField(Node node, Attribute elementField, Object to, Field field) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Node attribute = node.getAttributes().getNamedItem(elementField.name());

        if (attribute == null && elementField.nullable()) return;
        else if (attribute == null)
            throw new TranscriptionParsingException("Field \"" + elementField.name() + "\" can't be null", node);

        if (field.getType().isArray())
            PropertyUtils.setSimpleProperty(to, field.getName(), this.parseList(attribute.getNodeValue()));
        else
            PropertyUtils.setSimpleProperty(to, field.getName(),
                    field.getType().equals(GeneratedValue.class) ? ExpressionFactory.createExpression(attribute.getNodeValue(),
                            ReflectionUtils.getRawGenericType(field), node, namespace)
                            : field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ? Boolean.parseBoolean(attribute.getNodeValue())
                            : field.getType().equals(int.class) || field.getType().equals(Integer.class) ? Integer.parseInt(attribute.getNodeValue())
                            : attribute.getNodeValue()
            );
    }

    private String[] parseList(String value){
        return Arrays.stream(value.split(Syntax.LIST_SPLITERATOR)).map(String::trim).toArray(String[]::new);
    }
}
