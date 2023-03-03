package org.telegram.telegrise;

import org.junit.jupiter.api.Test;
import org.telegram.telegrise.annotations.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertThrows;

class ResourceInjectorTest {

    @Test
    void injectResources() {
        Integer integer = 123;
        List<String> strings = List.of("some", "strings");

        ResourceInjector resourceInjector = new ResourceInjector(integer, strings);

        CorrectInjectionExample obj = new CorrectInjectionExample();
        resourceInjector.injectResources(obj);

        assertEquals(integer, obj.integerInjected);
        assertEquals(strings, obj.strings);

        WrongInjectionExample wrong = new WrongInjectionExample();
        assertThrows(TelegRiseRuntimeException.class, () -> resourceInjector.injectResources(wrong));
    }

    private static class CorrectInjectionExample {
        @Resource
        public Number integerInjected;

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