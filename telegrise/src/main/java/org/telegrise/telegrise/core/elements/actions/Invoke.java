package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

/**
 * Invokes specified method reference or expression. This element doesn't make any API calls.
 * <pre>
 * {@code
 * <invoke method="#execute"/>
 * }
 *
 * @since 0.1
 */
@Element(name = "invoke")
@Getter @Setter @NoArgsConstructor
public class Invoke extends ActionElement{
    @Attribute(name = "method", nullable = false)
    private GeneratedValue<Void> method;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        method.generate(resourcePool);

        return null;
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
