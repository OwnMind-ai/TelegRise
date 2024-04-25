package org.telegram.telegrise;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class MediaCollector {
    private final Queue<Update> updatesQueue;
    @Setter
    private Duration delay = Duration.ofMillis(500);

    public MediaCollector(Queue<Update> updatesQueue) {
        this.updatesQueue = updatesQueue;
    }

    public List<Message> collect(Update first){
        if (!first.hasMessage() )
            throw new TelegRiseRuntimeException("Unable to collect mediagroup: first update does not store message");

        return this.collect(first.getMessage());
    }

    public List<Message> collect(Message first){
        if (first.getMediaGroupId() == null)
            throw new TelegRiseRuntimeException("Unable to collect mediagroup: first message doesn't have mediagroupID");

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

    private synchronized List<Message> extractMediaGroup(String mediagroupId){
        return this.updatesQueue.stream()
                .filter(update -> update.hasMessage() && mediagroupId.equals(update.getMessage().getMediaGroupId()))
                .peek(updatesQueue::remove)
                .map(Update::getMessage)
                .collect(Collectors.toList());
    }
}
