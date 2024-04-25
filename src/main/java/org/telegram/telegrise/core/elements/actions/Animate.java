package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.AnimationExecutor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "animate")
@Data @NoArgsConstructor
public class Animate implements ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "period", nullable = false)
    private GeneratedValue<Float> period;

    @Attribute(name = "parallel")
    private boolean parallel;

    @Attribute(name = "after")
    private GeneratedValue<ReturnConsumer> after;

    @Attribute(name = "until")
    private GeneratedValue<Boolean> until;

    @Attribute(name = "loops")
    private GeneratedValue<Integer> loops;

    @Attribute(name = "deleteAfter")
    private boolean deleteAfter = true;

    @InnerElement(nullable = false)
    private List<Frame> frames;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (frames.size() <= 1)
            throw new TranscriptionParsingException("Must have at least two frames or more", node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        AnimationExecutor executor = new AnimationExecutor(this, resourcePool.getSender(), resourcePool);
        executor.start();

        return null;
    }
}
