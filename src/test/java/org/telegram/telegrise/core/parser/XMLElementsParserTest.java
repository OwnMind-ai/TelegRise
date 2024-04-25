package org.telegram.telegrise.core.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(this.getClass().getClassLoader())), null);
        parser.load();
        Node node = toNode("<text parseMode=\"html\" entities=\"${java.util.Collections.singletonList(null)}\">val</text>");

        Text text = new Text("val", "html");
        text.setEntities(GeneratedValue.ofValue(java.util.Collections.singletonList(null)));
        Assertions.assertNull(text.getParseMode());
        assertElements(text, parser.parse(node), new ResourcePool());
    }

    @Test
    void parseSend() throws Exception {
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(), null);
        parser.load();

        Node node = toNode("<send chat=\"-1\" disableWebPagePreview=\"true\">\n" +
                "                    <text>Text</text>\n" +
                "                </send>");

        Send expected = new Send();
        expected.setText(new Text("Text", "html"));
        expected.setChatId(GeneratedValue.ofValue(-1L));
        expected.setDisableWebPagePreview(GeneratedValue.ofValue(true));

        assertElements(expected, parser.parse(node), new ResourcePool());
    }

    @Test
    void parseBranch() throws Exception{
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(), null);
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

        assertElements(expected, parser.parse(node), new ResourcePool());
    }

    @Test
    void parseTree() throws Exception{
        ApplicationNamespace namespace = new ApplicationNamespace(this.getClass().getClassLoader());
        namespace.addClass(this.getClass().getName());
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(this.getClass(), namespace), null);
        parser.load();

        Node node = toNode("<tree name=\"name\" predicate=\"true\" callbackTriggers=\"callback-data\" keys=\"first; second\" commands=\"example\"\n" +
                "              controller=\"XMLElementsParserTest\" type=\"reply\">\n" +
                "            <send chat=\"-1\">\n" +
                "               <text parseMode=\"markdown\">Text</text>\n" +
                "            </send>" +
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

        Send treeSend = new Send();
        treeSend.setText(new Text("Text", "markdown"));
        treeSend.setChatId(GeneratedValue.ofValue(-1L));

        Tree expected = new Tree();
        expected.setName("name");
        expected.setPredicate(GeneratedValue.ofValue(true));
        expected.setCallbackTriggers(new String[]{"callback-data"});
        expected.setKeys(new String[]{"first", "second"});
        expected.setCommands(new String[]{"example"});
        expected.setController(this.getClass());
        expected.setActions(List.of(treeSend));
        expected.setBranches(List.of(expectedBranch));

        assertElements(expected, parser.parse(node), new ResourcePool());
    }

    @Test
    void parseKeyboard() throws Exception {
        XMLElementsParser parser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(this.getClass().getClassLoader())), null);
        parser.load();

        Node node = toNode("<keyboard name=\"name\" type=\"inline\">\n" +
                "                    <row>\n" +
                "                        <button data=\"first\">First</button>\n" +
                "                        <button data=\"second\">Second</button>\n" +
                "                        <button data=\"third\" when=\"${false}\">Third</button>\n" +
                "                    </row>\n" +
                "                    <row><button url=\"url\">URL</button></row>\n" +
                "                </keyboard>");

        InlineKeyboardMarkup expected = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("First").callbackData("first").build(),
                        InlineKeyboardButton.builder().text("Second").callbackData("second").build()
                ),
                new InlineKeyboardRow(InlineKeyboardButton.builder().text("URL").url("url").build())
        ));

        assertEquals(expected, ((Keyboard) parser.parse(node)).createMarkup(new ResourcePool(null, null, null, new SessionMemoryImpl(0, null, null))));
    }

    public static void assertElements(TranscriptionElement expected, TranscriptionElement actual, ResourcePool pool){
        if (expected == actual)
            return;

        if(!expected.getClass().equals(actual.getClass()))
            fail(String.format("Elements %s and %s are instances of different types", expected.getClass().getCanonicalName(), actual.getClass().getCanonicalName()));

        Map<String, Field> expectedFields = Arrays.stream(expected.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Attribute.class) || f.isAnnotationPresent(InnerElement.class))
                .collect(Collectors.toMap(Field::getName, f -> f));

        Map<String, Field> actualFields = Arrays.stream(actual.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Attribute.class) || f.isAnnotationPresent(InnerElement.class))
                .collect(Collectors.toMap(Field::getName, f -> f));

        for (String name : expectedFields.keySet()) {
            try {
                if(!compareFields(expectedFields.get(name), expected, actualFields.get(name), actual, pool))
                    fail(String.format("Field '%s' does not match to expected '%s'", actualFields.get(name).get(actual), expectedFields.get(name).get(expected)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean compareFields(Field expected, TranscriptionElement expectedInstance, Field actual, TranscriptionElement actualInstance, ResourcePool pool) throws IllegalAccessException {
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
                assertElements(expectedList.get(i), actualList.get(i), pool);
            }

            return true;
        } else if (expected.getType().isArray()) {
            return Arrays.equals((Object[]) expected.get(expectedInstance), (Object[]) actual.get(actualInstance));
        } else if (!TranscriptionElement.class.isAssignableFrom(expected.getType()))
            return (expected.get(expectedInstance) == (actual.get(actualInstance))) ||
                (expected.getType().isAssignableFrom(GeneratedValue.class) && actual.getType().isAssignableFrom(GeneratedValue.class)
                    && ((GeneratedValue<?>) expected.get(expectedInstance)).equalsTo((GeneratedValue<?>) actual.get(actualInstance), pool))
                || (expected.get(expectedInstance).equals(actual.get(actualInstance)));
        else {
            assertElements((TranscriptionElement) expected.get(expectedInstance), (TranscriptionElement) actual.get(actualInstance), pool);
            return true;
        }
    }
}