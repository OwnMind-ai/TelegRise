package org.telegram.telegrise;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.telegram.telegrise.annotations.Resource;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.resources.ResourceFactory;
import org.telegram.telegrise.resources.ResourceInjector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertThrows;

class ResourceInjectorTest {

    @Test
    void injectResources() {
        Integer integer = 123;
        List<String> strings = List.of("some", "strings");

        ResourceInjector resourceInjector = new ResourceInjector(integer, strings);
        resourceInjector.addFactories(List.of(new ResourceFactory<Character>() {
            @Override
            public @NotNull Class<Character> gerResourceClass() {
                return Character.class;
            }

            @Override
            public Character getResource(Object target) {
                return '1';
            }
        }));

        CorrectInjectionExample obj = new CorrectInjectionExample();
        resourceInjector.injectResources(obj);

        assertEquals(integer, obj.integerInjected);
        assertEquals(strings, obj.strings);
        assertEquals('1', obj.charInjected);

        WrongInjectionExample wrong = new WrongInjectionExample();
        assertThrows(TelegRiseRuntimeException.class, () -> resourceInjector.injectResources(wrong));
    }

    private static class CorrectInjectionExample {
        @Resource
        public Number integerInjected;

        @Resource
        public Character charInjected;

        @Resource
        private List<String> strings;
    }

    private static class WrongInjectionExample{
        @Resource
        public Integer integer;

        @Resource
        public String string;
    }
}