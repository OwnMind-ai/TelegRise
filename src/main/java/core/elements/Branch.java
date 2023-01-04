package core.elements;

import core.InvocationList;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

public class Branch implements TranscriptionElement{
    private final Predicate<Update> when;
    private final InvocationList invocationList;

    public Branch(Predicate<Update> when, InvocationList invocationList) {
        this.when = when;
        this.invocationList = invocationList;
    }
}
