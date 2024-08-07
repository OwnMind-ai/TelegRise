package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.stream.Collectors;

@Element(name = "chatAction")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatAction extends NodeElement implements ActionElement{
    private static final Set<String> ACTIONS = Set.of("typing", "upload_photo", "record_video", "upload_video",
            "record_voice", "upload_voice", "upload_document", "choose_sticker", "find_location", "record_video_note", "upload_video_note");

    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    @Attribute(name = "action", nullable = false)
    private GeneratedValue<String> action;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<ReturnConsumer> returnConsumer;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (!action.validate(ACTIONS::contains))
            throw new TranscriptionParsingException("Unrecognized chat action. Chat action could be one of following: " +
                    ACTIONS.stream().sorted().collect(Collectors.joining(", ")), node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return SendChatAction.builder()
                .chatId(generateChatId(resourcePool))
                .messageThreadId(generateNullableProperty(messageThreadId, resourcePool))
                .action(action.generate(resourcePool))
                .build();
    }
}
