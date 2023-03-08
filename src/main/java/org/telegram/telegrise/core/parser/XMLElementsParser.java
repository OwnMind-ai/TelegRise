package org.telegram.telegrise.core.parser;

import lombok.Setter;
import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class XMLElementsParser {
    private static final String ELEMENTS_PACKAGE = "org.telegram.telegrise.core.elements";

    private static Set<Class<? extends TranscriptionElement>> loadClasses(){
        return new Reflections(ELEMENTS_PACKAGE).getSubTypesOf(TranscriptionElement.class);
    }

    private final Map<String, Class<? extends TranscriptionElement>> elements = new HashMap<>();
    @Setter
    private LocalNamespace namespace;

    public XMLElementsParser(LocalNamespace namespace){
        this.namespace = namespace;
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

        Arrays.stream(element.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ElementField.class))
                .sorted(Comparator.<Method>comparingDouble(m -> m.getAnnotation(ElementField.class).priority()).reversed())
                .forEach(m -> {
                    m.setAccessible(true);
                    assert Arrays.equals(m.getParameterTypes(), new Class<?>[]{Node.class, LocalNamespace.class});

                    try {
                        Object namespace = m.invoke(instance, node, this.namespace);

                        if (namespace instanceof LocalNamespace)
                            this.namespace = (LocalNamespace) namespace;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e.getTargetException());
                    }
                });

        // Parse fields
        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ElementField.class))
                .sorted(Comparator.<Field>comparingDouble(m -> m.getAnnotation(ElementField.class).priority()).reversed())
                .forEach(f -> {
                    try {
                        this.parseField(f, node, instance);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });

        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(InnerElement.class))
                .forEach(f -> this.parseInnerElement(node, f, instance));

        LocalNamespace newNamespace = instance.createNamespace(this.namespace.getApplicationNamespace());
        if (newNamespace != null)
            this.namespace = newNamespace;

        return instance;
    }
    
    private void parseInnerElement(Node node, Field field, TranscriptionElement instance){
        NodeList nodeList = node.getChildNodes();
        InnerElement fieldData = field.getAnnotation(InnerElement.class);
        Class<?> actualType = getActualType(field);

        Element innerElementData = actualType.getAnnotation(Element.class);

        LinkedList<String> nodeNames = new LinkedList<>();
        LinkedList<Node> fieldNodes = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            // Node name equals Element.name() or Element type is inherited
            if ((innerElementData != null && nodeList.item(i).getNodeName().equals(innerElementData.name()))
                    || (elements.containsKey(nodeList.item(i).getNodeName())
                    && actualType.isAssignableFrom(elements.get(nodeList.item(i).getNodeName()))))
                fieldNodes.add(nodeList.item(i));

            nodeNames.add(nodeList.item(i).getNodeName());
        }

        if (fieldNodes.isEmpty()){
            if (EmbeddableElement.class.isAssignableFrom(actualType)) {
                //FIXME optimize (use #text node name)
                if (Arrays.stream(field.getDeclaringClass().getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(InnerElement.class))
                        .map(f -> getActualType(f).getAnnotation(Element.class).name())
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

                    PropertyUtils.setSimpleProperty(instance, field.getName(), object);
                    return;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (fieldData.nullable()) return;
            else throw new TranscriptionParsingException("Field \"" + (innerElementData != null ? innerElementData.name() : field.getName()) + "\" can't be null", node);
        }

        try {
            if (List.class.isAssignableFrom(field.getType())) {
                PropertyUtils.setSimpleProperty(instance, field.getName(), fieldNodes.stream()
                        .map(n -> {
                            try { return parse(n); } catch (Exception e) { throw new RuntimeException(e); }
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
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getActualType(Field field) {
        return List.class.isAssignableFrom(field.getType()) ?
                ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])
                : field.getType();
    }

    private void parseField(Field field, Node node, TranscriptionElement instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        field.setAccessible(true);
        ElementField elementData = field.getAnnotation(ElementField.class);
        Class<?> fieldType = field.getType();

        if (elementData.expression() && !fieldType.equals(GeneratedValue.class))
            throw new RuntimeException("Trying to assign non-generated value to GeneratedValue<?> type field");

        if(elementData.isTextContext())
            field.set(instance, node.getTextContent());
        else if (!elementData.name().equals("")) {
            this.setField(node, elementData, instance, field);
        }
    }

    private void setField(Node node, ElementField elementField, Object to, Field field) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Node attribute = node.getAttributes().getNamedItem(elementField.name());

        if (attribute == null && elementField.nullable()) return;
        else if (attribute == null)
            throw new TranscriptionParsingException("Field \"" + elementField.name() + "\" can't be null", node);

        if (field.getType().isArray())
            PropertyUtils.setSimpleProperty(to, field.getName(), this.parseList(attribute.getNodeValue()));
        else
            PropertyUtils.setSimpleProperty(to, field.getName(),
                    elementField.expression() ? ExpressionFactory.createExpression(attribute.getNodeValue(),
                            (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], node, namespace)  // Gets an actual type of GeneratedValue
                            : attribute.getNodeValue()
            );
    }

    private String[] parseList(String value){
        return Arrays.stream(value.split(Syntax.LIST_SPLITERATOR)).map(String::trim).toArray(String[]::new);
    }
}
