package org.telegrise.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.elements.base.LinkableElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.head.Link;
import org.telegrise.telegrise.core.parser.*;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;

/**
 * An element that stores global keyboards that can be linked using {@code <link>}.
 * All keyboards in this element must have a name.
 * If {@code context} is defined with existing tree controller name,
 * keyboards will be able to use its method references.
 *
 * @since 0.1
 * @see Keyboard
 * @see Link
 */
@Element(name="keyboards")
@Getter @Setter @NoArgsConstructor
public class Keyboards extends NodeElement implements LinkableElement {
    @InnerElement(nullable = false)
    private List<Keyboard> keyboards;

    /**
     * A name of a tree controller that will be used to compile method references
     */
    @Attribute(name = "context")
    private String context;

    @Override
    public void validate(TranscriptionMemory memory) {
        if(keyboards.stream().noneMatch(k -> k.getName() != null))
            throw new TranscriptionParsingException("Child elements must have a name in order to be linked", node);
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        if (parentTree != null ) return null;
        if (context == null) return global.emptyLocal();

        Class<?> controller = global.getClass(context);
        if (!controller.isAnnotationPresent(TreeController.class))
            throw new TranscriptionParsingException("Tree uses a controller the class of which is not annotated with @TreeController", node);

        return new LocalNamespace(controller, global);
    }
}
