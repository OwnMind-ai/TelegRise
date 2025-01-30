package org.telegrise.telegrise.core.elements.text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.ApplicationNamespace;
import org.telegrise.telegrise.core.elements.LinkableElement;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;

@Element(name = "texts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Texts extends NodeElement implements LinkableElement {
    @InnerElement(nullable = false)
    private List<Text> keyboards;

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
}