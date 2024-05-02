package org.telegram.telegrise.core.parser;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.Syntax;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.utils.ReflectionUtils;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XMLElementsParser {
    private static final String ELEMENTS_PACKAGE = "org.telegram.telegrise.core.elements";

    private static Set<Class<? extends TranscriptionElement>> loadClasses(){
        return new Reflections(ELEMENTS_PACKAGE).getSubTypesOf(TranscriptionElement.class);
    }

    private final Map<String, Class<? extends TranscriptionElement>> elements = new HashMap<>();
    @Setter
    private LocalNamespace namespace;
    @Getter
    private final TranscriptionMemory transcriptionMemory = new TranscriptionMemory();
    @Getter
    private final File rootDirectory;

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

    public TranscriptionElement parse(@NotNull Node node) throws Exception {
        Class<? extends TranscriptionElement> element = this.elements.get(node.getNodeName());
        TranscriptionElement instance = element.getConstructor().newInstance();

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

        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(InnerElement.class))
                .sorted(Comparator.<Field>comparingDouble(f -> f.getAnnotation(InnerElement.class).priority()).reversed())
                .forEach(f -> this.parseInnerElement(node, f, instance, expected));

        if (!expected.isEmpty())
            throw new TranscriptionParsingException("Unrecognized elements: " + String.join(", ", expected), node);

        finishElement(instance, node);

        LocalNamespace newNamespace = instance.createNamespace(this.namespace.getApplicationNamespace());
        if (newNamespace != null)
            this.namespace = newNamespace;

        return instance;
    }

    private void finishElement(TranscriptionElement instance, @NotNull Node node) {
        // ORDER MATTERS:
        if(instance.getClass().getAnnotation(Element.class).validateAfterParsing())
            this.transcriptionMemory.getPendingValidation().add(Pair.of(instance, node));
        else
            instance.validate(node, transcriptionMemory);

        instance.load(transcriptionMemory);
        if (instance instanceof StorableElement)
            ((StorableElement) instance).store(transcriptionMemory);
    }

    private void parseMethod(TranscriptionElement instance, Map<Class<?>, Object> resourcesMap, Method method) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        Object[] parameters = Arrays.stream(method.getParameterTypes()).map(resourcesMap::get).toArray();

        Object result = method.invoke(instance, parameters);

        if (result instanceof LocalNamespace)
            this.namespace = (LocalNamespace) result;
    }

    private void parseInnerElement(Node node, Field field, TranscriptionElement instance, Set<String> expected){
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
                if (nodeList.getLength() == 0) return;
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
                    if (fieldData.nullable()) return;
                    else {
                        assert innerElementData != null;
                        throw new TranscriptionParsingException("Embedded element '" + innerElementData.name() + "' is not allowed here, use <" + innerElementData.name() + "> instead", node);
                    }
                }

                try {
                    EmbeddableElement object = (EmbeddableElement) actualType.getConstructor().newInstance();
                    object.parse(node, this.namespace);

                    finishElement(object, node);

                    PropertyUtils.setSimpleProperty(instance, field.getName(), object);
                    return;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new TelegRiseInternalException(e);
                }
            }
            else if (fieldData.nullable()) return;
            else throw new TranscriptionParsingException("Field \"" + (innerElementData != null ? innerElementData.name() : field.getName()) + "\" can't be null", node);
        }

        try {
            if (List.class.isAssignableFrom(field.getType())) {
                PropertyUtils.setSimpleProperty(instance, field.getName(), fieldNodes.stream()
                        .map(n -> {
                            try { return parse(n); } catch (Exception e) { throw new TelegRiseInternalException(e); }
                        }).collect(Collectors.toUnmodifiableList())
                );
            } else {
                if (fieldNodes.size() == 1)
                    PropertyUtils.setSimpleProperty(instance, field.getName(), this.parse(fieldNodes.getFirst()));
                else
                    throw new TranscriptionParsingException(
                            "Field \"" + (innerElementData != null ? innerElementData.name() : field.getName()) + "\" has more than one definition", node);
            }
        } catch (Exception e) {
            throw new TelegRiseInternalException(e);
        }
    }

    private void parseField(Field field, Node node, TranscriptionElement instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
