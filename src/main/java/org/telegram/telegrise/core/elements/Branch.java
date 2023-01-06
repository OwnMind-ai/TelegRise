package org.telegram.telegrise.core.elements;

import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.invocation.InvocationList;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.parser.Element;

import java.util.function.Predicate;

@Element(name = "branch")
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    private Predicate<Update> when;
    private InvocationList invocationList;

    public Branch(Predicate<Update> when, InvocationList invocationList) {
        this.when = when;
        this.invocationList = invocationList;
    }
}
