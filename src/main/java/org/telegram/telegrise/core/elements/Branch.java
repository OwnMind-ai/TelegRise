package org.telegram.telegrise.core.elements;

import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;

@Element(name = "branch")
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    @ElementField(name = "when", expression = true, nullable = false)
    private GeneratedValue<Boolean> when;

    @ElementField(name = "handler")
    private GeneratedValue<Void> toInvoke;

    // TODO InnerElements of ActionElement type
}
