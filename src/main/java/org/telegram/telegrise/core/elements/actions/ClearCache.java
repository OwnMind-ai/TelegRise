package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.caching.MethodReferenceCache;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.Map;

@Element(name = "clearCache")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
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
