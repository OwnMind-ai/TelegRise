package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Element(name = "wait")
@Getter @Setter @NoArgsConstructor
public class Wait extends ActionElement{
    @Attribute(name = "timeout", nullable = false)
    private GeneratedValue<Double> timeout;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "listener")
    private GeneratedValue<Consumer<Update>> listener;

    @Attribute(name = "onInterrupted")
    private GeneratedValue<Consumer<InterruptedException>> onInterrupted;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        AtomicBoolean running = new AtomicBoolean(true);
        Thread listenerThread = null;
        if (listener != null){
            listenerThread = this.createListenerTask(listener.generate(resourcePool), resourcePool.getUpdates(), running);
            listenerThread.start();
        }

        try {
            Thread.sleep((long) (timeout.generate(resourcePool) * 1000));
            running.set(false);

            if(listenerThread != null && listenerThread.isAlive())
                listenerThread.interrupt();
        } catch (InterruptedException e) {
            if(onInterrupted != null)
                onInterrupted.generate(resourcePool).accept(e);
            else
                throw new TelegRiseRuntimeException("Waiting action was interrupted:" + e.getMessage(), node);
        }

        return null;
    }

    private Thread createListenerTask(Consumer<Update> listener, BlockingQueue<Update> updates, AtomicBoolean running) {
        return new Thread(() -> {
            try {
                while (running.get()) {
                    listener.accept(updates.take());
                }
            } catch (InterruptedException ignored) {}
        });
    }


    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
