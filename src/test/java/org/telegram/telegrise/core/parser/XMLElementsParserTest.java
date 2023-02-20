package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.Send;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class XMLElementsParserTest {
    public static Node toNode(String node){
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(node.getBytes(StandardCharsets.UTF_8)))
                    .getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void parseText() throws Exception {
        XMLElementsParser parser = new XMLElementsParser();
        parser.load();
        Node node = toNode("<text parseMode=\"html\">val</text>");

        assertElements(new Text("val", "html"), parser.parse(node));
    }

    @Test
    void parseSend() throws Exception {
        XMLElementsParser parser = new XMLElementsParser();
        parser.load();

        Node node = toNode("<send chat=\"-1\" disableWebPreview=\"true\">\n" +
                "                    <text>Text</text>\n" +
                "                </send>");

        Send expected = new Send();
        expected.setText(new Text("Text", "html"));
        expected.setChatId(GeneratedValue.ofValue(-1L));
        expected.setDisableWebPreview(GeneratedValue.ofValue(true));

        assertElements(expected, parser.parse(node));
    }

    @Test
    void parseBranch() throws Exception{
        XMLElementsParser parser = new XMLElementsParser();
        parser.load();

        Node node = toNode("<branch when=\"true\">\n" +
                "                <send chat=\"-1\">\n" +
                "                    <text>Text</text>\n" +
                "                </send>\n" +
                "            </branch>");

        Send expectedSend = new Send();
        expectedSend.setText(new Text("Text", "html"));
        expectedSend.setChatId(GeneratedValue.ofValue(-1L));

        Branch expected = new Branch();
        expected.setWhen(GeneratedValue.ofValue(true));
        expected.setActions(List.of(expectedSend));

        assertElements(expected, parser.parse(node));
    }

    @Test
    void parseTree() throws Exception{
        XMLElementsParser parser = new XMLElementsParser();
        parser.load();

        Node node = toNode("<tree name=\"name\" predicate=\"true\" callbackTriggers=\"callback-data\" keys=\"first; second\" commands=\"example\"\n" +
                "              handler=\"SimpleHandler\" type=\"reply\">\n" +
                "            <text parseMode=\"markdown\">\n" +
                "               Text\n" +
                "            </text>" +
                "            <branch when=\"true\">\n" +
                "                <send chat=\"-1\">\n" +
                "                    <text>Text</text>\n" +
                "                </send>\n" +
                "                <send chat=\"-1\">\n" +
                "                    <text>Text</text>\n" +
                "                </send>\n"+
                "            </branch>" +
                "       </tree>");

        Send expectedSend = new Send();
        expectedSend.setText(new Text("Text", "html"));
        expectedSend.setChatId(GeneratedValue.ofValue(-1L));

        Branch expectedBranch = new Branch();
        expectedBranch.setWhen(GeneratedValue.ofValue(true));
        expectedBranch.setActions(List.of(expectedSend, expectedSend));

        Tree expected = new Tree();
        expected.setName("name");
        expected.setPredicate(GeneratedValue.ofValue(true));
        expected.setCallbackTriggers(new String[]{"callback-data"});
        expected.setKeys(new String[]{"first", "second"});
        expected.setCommands(new String[]{"example"});
        expected.setHandlerName("SimpleHandler");
        expected.setType("reply");
        expected.setText(new Text("Text", "markdown"));
        expected.setBranches(List.of(expectedBranch));

        assertElements(expected, parser.parse(node));
    }

    public static void assertElements(TranscriptionElement expected, TranscriptionElement actual){
        if(!expected.getClass().equals(actual.getClass()))
            fail(String.format("Elements %s and %s are instances of different types", expected.getClass().getCanonicalName(), actual.getClass().getCanonicalName()));

        Map<String, Field> expectedFields = Arrays.stream(expected.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ElementField.class) || f.isAnnotationPresent(InnerElement.class))
                .collect(Collectors.toMap(Field::getName, f -> f));

        Map<String, Field> actualFields = Arrays.stream(actual.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ElementField.class) || f.isAnnotationPresent(InnerElement.class))
                .collect(Collectors.toMap(Field::getName, f -> f));

        for (String name : expectedFields.keySet()) {
            try {
                if(!compareFields(expectedFields.get(name), expected, actualFields.get(name), actual))
                    fail(String.format("Field '%s' does not match to expected '%s'", actualFields.get(name).get(actual).toString(), expectedFields.get(name).get(expected).toString()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean compareFields(Field expected, TranscriptionElement expectedInstance, Field actual, TranscriptionElement actualInstance) throws IllegalAccessException {
        expected.setAccessible(true);
        actual.setAccessible(true);

        if (List.class.isAssignableFrom(expected.getType())
                && TranscriptionElement.class.isAssignableFrom(((Class<?>) ((ParameterizedType) expected.getGenericType()).getActualTypeArguments()[0]))
                && TranscriptionElement.class.isAssignableFrom(((Class<?>) ((ParameterizedType) actual.getGenericType()).getActualTypeArguments()[0]))) {
            @SuppressWarnings("unchecked") List<TranscriptionElement> expectedList = (List<TranscriptionElement>) expected.get(expectedInstance);
            @SuppressWarnings("unchecked") List<TranscriptionElement> actualList = (List<TranscriptionElement>) actual.get(actualInstance);

            if (expectedList == actualList) return true;
            if((expectedList != null && actualList != null) && expectedList.size() != actualList.size()) return false;

            for (int i = 0; i < Objects.requireNonNull(expectedList).size(); i++) {
                assert actualList != null;
                assertElements(expectedList.get(i), actualList.get(i));
            }

            return true;
        } else if (expected.getType().isArray()) {
            return Arrays.equals((Object[]) expected.get(expectedInstance), (Object[]) actual.get(actualInstance));
        } else if (!TranscriptionElement.class.isAssignableFrom(expected.getType()))
            return (expected.get(expectedInstance) == (actual.get(actualInstance))) ||
                (expected.getType().isAssignableFrom(GeneratedValue.class) && actual.getType().isAssignableFrom(GeneratedValue.class)
                    && ((GeneratedValue<?>) expected.get(expectedInstance)).equalsTo((GeneratedValue<?>) actual.get(actualInstance), new ResourcePool()))
                || (expected.get(expectedInstance).equals(actual.get(actualInstance)));
        else {
            assertElements((TranscriptionElement) expected.get(expectedInstance), (TranscriptionElement) actual.get(actualInstance));
            return true;
        }
    }
}