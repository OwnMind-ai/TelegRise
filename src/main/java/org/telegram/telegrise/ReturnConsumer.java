package org.telegram.telegrise;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.MessageId;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

//FIXME looks disgusting, I need more sleep
public interface ReturnConsumer {
    static ReturnConsumer message(Consumer<Message> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeMessage(Message message) {
                consumer.accept(message);
            }
        };
    }

    static ReturnConsumer messageList(Consumer<List<Message>> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeMessageList(List<Message> list) {
                consumer.accept(Collections.unmodifiableList(list));
            }
        };
    }

    static ReturnConsumer messageId(Consumer<MessageId> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeMessageId(MessageId id) {
                consumer.accept(id);
            }
        };
    }

    static ReturnConsumer success(Consumer<Boolean> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeBoolean(boolean b) {
                consumer.accept(b);
            }
        };
    }

    static ReturnConsumer inviteLink(Consumer<ChatInviteLink> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeInviteLink(ChatInviteLink link) {
                consumer.accept(link);
            }
        };
    }

    static ReturnConsumer any(Consumer<Object> consumer){
        return new ReturnConsumer(){
            @Override
            public void consumeAny(Object obj) {
                consumer.accept(obj);
            }
        };
    }

    default void consume(@NotNull Object object){
        if (object instanceof Message)
            this.consumeMessage((Message) object);
        else if (object instanceof List<?>)
            //noinspection unchecked
            this.consumeMessageList((List<Message>) object);
        else if(object instanceof MessageId)
            this.consumeMessageId((MessageId) object);
        else if (object instanceof Boolean)
            this.consumeBoolean((boolean) object);
        else if (object instanceof ChatInviteLink)
            this.consumeInviteLink((ChatInviteLink) object);
        else
            this.consumeAny(object);
    }

    default void consumeMessage(Message message) {
        throw new TelegRiseRuntimeException("Missing consumer for Message type");
    }

    default void consumeMessageList(List<Message> messageList) {
        throw new TelegRiseRuntimeException("Missing consumer for List<Message> type");
    }

    default void consumeMessageId(MessageId id){
        throw new TelegRiseRuntimeException("Missing consumer for MessageId type");
    }

    default void consumeBoolean(boolean b){
        throw new TelegRiseRuntimeException("Missing consumer for Boolean type");
    }

    default void consumeInviteLink(ChatInviteLink link){
        throw new TelegRiseRuntimeException("Missing consumer for ChatInviteLink type");
    }

    default void consumeAny(Object result){
        throw new TelegRiseRuntimeException("Missing consumer for Object type");
    }
}
