package org.telegram.telegrise.core.elements.text;

import java.util.List;

import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.elements.LinkableElement;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

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