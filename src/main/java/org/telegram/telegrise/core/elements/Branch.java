package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "branch")
@Data
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    @ElementField(name = "when", expression = true, nullable = false)
    private GeneratedValue<Boolean> when;

    //TODO keys & callbacks

    @ElementField(name = "handler")
    private GeneratedValue<Void> toInvoke; // TODO implement

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private List<Branch> branches;
}
