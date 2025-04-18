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
import org.telegrise.telegrise.core.utils.ReflectionUtils;

import java.util.Map;

/**
 * Clears cache of a method with a specified name. This element doesn't make any API calls.
 * <pre>
 * {@code
 * <invoke method="#cachableMethod"/>
 * <clearCache method="cachableMethod"/>
 * }
 * </pre>
 *
 * @since 0.6
 */
@Element(name = "clearCache")
@Getter @Setter
@NoArgsConstructor
public class ClearCache extends ActionElement{
    /**
     * Name of the method cache of which needs to be cleared
     */
    @Attribute(name = "method", nullable = false)
    private GeneratedValue<String> method;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        String methodName = method.generate(resourcePool);
        Class<?> clazz = ReflectionUtils.getClass(resourcePool.getCurrentExecutor().getControllerInstance());

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