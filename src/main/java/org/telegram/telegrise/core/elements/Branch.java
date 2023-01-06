package org.telegram.telegrise.core.elements;

import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.invocation.InvocationList;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.w3c.dom.Node;

@Element(name = "branch")
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    @ElementField(name = "when", expression = true, nullable = false)
    private GeneratedValue<Boolean> when;
    private InvocationList invocationList;

    @ElementField(nullable = false)
    private void setInvocationList(Node node){
        //TODO
    }
}
