package org.telegrise.telegrise;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.resources.ResourceFactory;
import org.telegrise.telegrise.resources.ResourceInjector;

import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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