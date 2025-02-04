package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.caching.MethodReferenceCache;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.Map;

@Element(name = "clearCache")
@Getter @Setter
@NoArgsConstructor
public class ClearCache extends ActionElement{
    @Attribute(name = "method", nullable = false)
    private GeneratedValue<String> method;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        String methodName = method.generate(resourcePool);
        Class<?> clazz = resourcePool.getCurrentExecutor().getControllerInstance().getClass();

        resourcePool.getMemory().getCacheMap().entrySet().stream()
                .filter(e -> e.getKey().getDeclaringClass().equals(clazz) && e.getKey().getMethod().getName().equals(methodName))
                .map(Map.Entry::getValue)
                .findFirst()
                .ifPresent(MethodReferenceCache::clear);

        return null;
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
