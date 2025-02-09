package org.telegrise.telegrise;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * An object that is used to collect media groups from incoming updates based on {@code mediagroupId}.
 * Injectable using {@link org.telegrise.telegrise.annotations.Resource Resource} annotation.
 *
 * @since 0.3
 */
public final class MediaCollector {
    private final Queue<Update> updatesQueue;
    @Setter
    private Duration delay = Duration.ofMillis(500);

    public MediaCollector(Queue<Update> updatesQueue) {
        this.updatesQueue = updatesQueue;
    }

    /**
     * Collects all messages that have the same {@code mediagroupId} as the message in provided {@code Update} instance.
     *
     * @param first first update with a message
     * @return list of all messages with the same {@code mediagroupId}, including the {@code first}
     * @throws TelegRiseRuntimeException {@code first} has no message or its message has no {@code mediagroupId}
     */
    public List<Message> collect(Update first){
        if (!first.hasMessage() )
            throw new TelegRiseRuntimeException("Unable to collect media group: first update does not store message");

        return this.collect(first.getMessage());
    }

    /**
     * Collects all messages that have the same {@code mediagroupId} as in provided {@code Message} instance.
     *
     * @param first first message
     * @return list of all messages with the same {@code mediagroupId}, including the {@code first}
     * @throws TelegRiseRuntimeException {@code first} has no {@code mediagroupId}
     */
    public List<Message> collect(Message first){
        if (first.getMediaGroupId() == null)
            throw new TelegRiseRuntimeException("Unable to collect media group: first message doesn't have mediagroupID");

        String mediagroupId = first.getMediaGroupId();
        List<Message> result = new LinkedList<>();
        result.add(first);


        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            throw new TelegRiseRuntimeException("MediaCollector thread was interrupted while waiting for upcoming updates: " + e.getMessage());
        }

        result.addAll(this.extractMediaGroup(mediagroupId));

        if (result.size() <= 1)
            throw new TelegRiseRuntimeException("MediaCollector did not find any related massages within " + delay.toMillis() + "ms");

        return Collections.unmodifiableList(result);
    }

    private List<Message> extractMediaGroup(String mediagroupId){
        synchronized (this.updatesQueue) {
            return this.updatesQueue.stream()
                    .filter(u -> u.hasMessage() && mediagroupId.equals(u.getMessage().getMediaGroupId()))
                    .peek(updatesQueue::remove)
                    .map(Update::getMessage)
                    .collect(Collectors.toList());
        }
    }
}
