package org.telegram.telegrise.core.parser;

import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class XMLElementsParser {
    private static final String ELEMENTS_PACKAGE = "org.telegram.telegrise.core.elements";
    private static final String LIST_SPLITERATOR = "(?<!\\\\);";

    private static Set<Class<? extends TranscriptionElement>> loadClasses(){
        return new Reflections(ELEMENTS_PACKAGE).getSubTypesOf(TranscriptionElement.class);
    }

    private final Map<String, Class<? extends TranscriptionElement>> elements = new HashMap<>();

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

        // Parse fields
        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ElementField.class))
                .forEach(f -> {
                    try {
                        this.parseField(f, node, instance);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });

        Arrays.stream(element.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ElementField.class))
                .forEach(m -> {
                    m.setAccessible(true);
                    assert Arrays.equals(m.getParameterTypes(), new Class<?>[]{Node.class});

                    try {
                        m.invoke(instance, node);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

        Arrays.stream(element.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(InnerElement.class))
                .forEach(f -> this.parseInnerElement(node, f, instance));

        return instance;
    }
    
    private void parseInnerElement(Node node, Field field, TranscriptionElement instance){
        NodeList nodeList = node.getChildNodes();
        InnerElement fieldData = field.getAnnotation(InnerElement.class);
        Element innerElementData = field.getType().getAnnotation(Element.class);

        LinkedList<Node> fieldNodes = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++)
            if (nodeList.item(i).getNodeName().equals(innerElementData.name()))
                fieldNodes.add(nodeList.item(i));

        if (fieldNodes.isEmpty() && fieldData.nullable()) return;
        else if (fieldNodes.isEmpty())
            throw new TranscriptionParsingException("Field \"" + innerElementData.name() + "\" can't be null", node);

        try {
            //TODO need to be tested
            if (field.getType().isInstance(List.class)) {
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
                            "Field \"" + innerElementData.name() + "\" has more than one definition", node);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                    elementField.expression() ? ExpressionFactory.parseExpression(attribute.getNodeValue(),
                            (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])  // Gets an actual type of GeneratedValue
                            : attribute.getNodeValue()
            );
    }

    private String[] parseList(String value){
        return value.split(LIST_SPLITERATOR);
    }
}
