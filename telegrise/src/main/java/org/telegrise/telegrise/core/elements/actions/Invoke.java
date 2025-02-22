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
 * <invoke method='${System.out.println("Hello!")}'/>
 * }
 * </pre>
 *
 * @since 0.1
 */
@Element(name = "invoke")
@Getter @Setter @NoArgsConstructor
public class Invoke extends ActionElement{
    /**
     * Expression to be executed
     */
    @Attribute(name = "method", nullable = false)
    private GeneratedValue<Void> method;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link Exception} to handle the exception.
     */
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
