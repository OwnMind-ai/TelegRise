package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "invoke")
@Data @NoArgsConstructor
public class Invoke implements ActionElement{
    @Attribute(name = "method")
    private GeneratedValue<Void> method;

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
