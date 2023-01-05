package org.telegram.telegrise.core;

import org.telegram.telegrise.core.elements.Invocable;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class InvocationList {
    private final List<Invocable> invocableList;
    private final Variables variables;

    public InvocationList(List<Invocable> invocableList, Variables variables) {
        this.invocableList = invocableList;
        this.variables = variables;
    }

    public void invoke(Update update) throws Exception {
        for (Invocable invocable : this.invocableList) invocable.run(update, this.variables);
    }
}
