package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.builtin.BuiltinReferences;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Optional;

/**
 * Use this method to delete a message, including service messages.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#deletemessage">deleteMessage</a> method.
 * {@link MessageUtils#getChat ChatId} and {@link MessageUtils#getMessageId messageId} are automatically extracted from the incoming update,
 * but can be specified if needed.
 * <p>
 * If {@link SessionMemory#putToRegistry registry} name is specified,
 * this element will delete all messages in specified registry at once using
 * <a href="https://core.telegram.org/bots/api#deletemessages">deleteMessages</a> method.
 * <pre>
 * {@code
 * <delete/>
 * <delete registry="registryName"/>
 * }
 * </pre>
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#deletemessage">Telegram API: deleteMessage</a>
 * @see SessionMemory#putToRegistry
 * @see BuiltinReferences#register
 */
@Element(name = "delete")
@Getter @Setter @NoArgsConstructor
public class Delete extends ActionElement{
    /**
     * Unique identifier for the target chat
     */
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Identifier of the message to delete
     */
    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    /**
     * Name of the registry to delete messages from
     */
    @Attribute(name = "registry")
    private GeneratedValue<String> registry;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        if (registry != null) {
            String name = registry.generate(resourcePool);
            List<Message> register = resourcePool.getMemory().clearRegistry(name);
            
            if (register.isEmpty()) return null;
           
            Long chatId = this.generateChatId(resourcePool);
            return DeleteMessages.builder()
                    .chatId(chatId)
                    .messageIds(register.stream()
                        .peek(m -> { if (!m.getChatId().equals(chatId))  // TODO perhaps it is possible just to delete from these chats (groupingBy chatId and then execute deletes through pool.getSender())
                            throw new TelegRiseRuntimeException("Message from the register is in the different chat (%d) then specified (%d): %s".formatted(m.getChatId(), chatId, m), node); })
                        .map(Message::getMessageId).distinct().toList())
                    .build();
        }

        return DeleteMessage.builder()
                .chatId(this.generateChatId(resourcePool))
                .messageId(messageId != null ? messageId.generate(resourcePool) :
                        Optional.ofNullable(MessageUtils.getMessageId(resourcePool.getUpdate()))
                                .orElseThrow(() -> new TelegRiseRuntimeException("Unable to extract message ID for deletion action", node)))
                .build();
    }
}
