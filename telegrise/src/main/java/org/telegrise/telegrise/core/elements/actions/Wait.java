package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Waits specified number of seconds. This element doesn't make any API calls.
 * <p>
 * If {@code listener} attribute specified, provided consumer will handle incoming updates during the waiting time.
 * <pre>
 * {@code
 * <wait timeout="10" listener="#getWarningConsumer"/>
 * }
 *
 * @since 0.1
 */
@Element(name = "wait")
@Getter @Setter @NoArgsConstructor
public class Wait extends ActionElement{
    /**
     * Number of seconds to sleep for.
     */
    @Attribute(name = "timeout", nullable = false)
    private GeneratedValue<Double> timeout;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * If defined, provided consumer will handle incoming updates during a timeout period.
     */
    @Attribute(name = "listener")
    private GeneratedValue<Consumer<Update>> listener;

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
