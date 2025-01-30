package org.telegrise.telegrise.bot.controllers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.MediaCollector;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.caching.CachingStrategy;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.stream.Collectors;

@TreeController
public class MediaGroupsController {
    @Resource
    private MediaCollector mediaCollector;

    // CACHING IS IMPORTANT HERE!!
    @Reference(caching = CachingStrategy.UPDATE)
    private List<Message> mediaGroupReceived(Update update){
        if (!update.hasMessage() || update.getMessage().getMediaGroupId() == null) return null;

        return mediaCollector.collect(update);
    }

    @Reference
    private List<InputMedia> toInputMedias(List<Message> messages){
        return messages.stream().map(MessageUtils::toInputMedia).toList();
    }

    @Reference
    private String mediaInfo(List<Message> messages){
        return messages.stream().map(m -> {
            if (m.hasPhoto()) {
                var photo = m.getPhoto().getLast();
                return """
                        Unique ID: <code>%s</code>
                        Size: <code>%d</code>
                        Dimensions: <code>%dx%d</code>
                        """.formatted(photo.getFileUniqueId(), photo.getFileSize(), photo.getWidth(), photo.getHeight());
            } else return """
                        Unique ID: <code>%s</code>
                        Size: <code>%d</code>
                        Dimensions: <code>%dx%d</code>
                        Duration: <code>%d</code>
                        """.formatted(m.getVideo().getFileUniqueId(), m.getVideo().getFileSize(),
                    m.getVideo().getWidth(), m.getVideo().getHeight(), m.getVideo().getDuration());
        }).collect(Collectors.joining("\n"));
    }
}
