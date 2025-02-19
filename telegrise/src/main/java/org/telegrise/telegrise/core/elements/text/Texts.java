package org.telegrise.telegrise.core.elements.text;

import lombok.AllArgsConstructor;
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
 * An element that stores global text that can be linked using {@code <link>}.
 * All text elements in this element must have a name.
 * If {@code context} is defined with existing tree controller name, texts will be able to use its method references.
 *
 * @since 0.1
 * @see Text
 * @see Link
 */
@Element(name = "texts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Texts extends NodeElement implements LinkableElement {
    @InnerElement(nullable = false)
    private List<Text> keyboards;

    /**
     * A name of a tree controller that will be used to compile method references
     */
    @Attribute(name = "context")
    private String context;

    @Override
    public void validate(TranscriptionMemory memory, ApplicationNamespace namespace) {
        try{
            if (context != null) 
                namespace.getClass(context);
        } catch (Exception e){
            throw new TranscriptionParsingException(e.getMessage(), node);
        }

        if(keyboards.stream().noneMatch(k -> k.getName() != null))
            throw new TranscriptionParsingException("Child elements must have a name in order to be linked", node);
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        if (context == null) return global.emptyLocal();

        Class<?> controller = global.getClass(context);
        if (!controller.isAnnotationPresent(TreeController.class))
            throw new TranscriptionParsingException("Tree uses a controller the class of which is not annotated with @TreeController", node);

        return new LocalNamespace(controller, global);
    }
}