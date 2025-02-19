package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.AnimationExecutor;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;

/**
 * Use this element to create text animations.
 * <p>
 * Animations can be
 * customized with options such as duration, conditions for starting and
 * stopping, repetition behavior, and a sequence of frames defining the
 * animation steps.
 * 
 * <p>
 * Loading animation example:
 * <pre>
 * {@code
 * <animate period="0.5" until="#loadFinished">
 *      <frame>Loading /</frame>
 *      <frame>Loading -</frame>
 *      <frame>Loading \</frame>
 *      <frame>Loading |</frame>
 * </animate>
 * }
 * </pre>
 * @since 0.1
 */
@Element(name = "animate")
@Getter @Setter @NoArgsConstructor
public class Animate extends ActionElement{
    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Number of seconds between frames.
     */
    @Attribute(name = "period", nullable = false)
    private GeneratedValue<Float> period;

    /**
     * If true, this element won't halt tree/branch flow during execution.
     */
    @Attribute(name = "parallel")
    private boolean parallel;

    /**
     * Expression to execute after animation is finished.
     */
    @Attribute(name = "after")
    private GeneratedValue<Void> after;

    /**
     * An expression what will be used between every frame to determine if animation must be finished
     */
    @Attribute(name = "until")
    private GeneratedValue<Boolean> until;

    /**
     * Number of iterations of frames to execute.
     */
    @Attribute(name = "loops")
    private GeneratedValue<Integer> loops;

    /**
     * If true (by default), the message of the animation will be deleted after the animation finishes
     */
    @Attribute(name = "deleteAfter")
    private boolean deleteAfter = true;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @InnerElement(nullable = false)
    private List<Frame> frames;

    @Override
    public void validate(TranscriptionMemory memory) {
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
