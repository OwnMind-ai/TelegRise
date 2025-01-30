package org.telegrise.telegrise;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.annotations.Handler;

/**
 * Represents a bot's update handler.
 * All handlers <b>must</b> be annotated with {@link org.telegrise.telegrise.annotations.Handler @Handler} annotation.
 * <p>
 * Update handlers are objects that allow handling Telegram updates in the simplest way:
 * if <code>canHandle</code> method return true, the <code>handle</code> method will be executed.
 * Handlers are the first to be looked at when deciding how to process the update
 * (before the trees, unless specified otherwise).
 * They retain their instance once executed for each session separately
 * (unless marked as {@link Handler#independent() independent}),
 * so you can use <b>local class fields</b> to keep any necessary state.
 * Resources can be injected in instances of this interface.
 * <p>
 * Additional behavior of handlers can be configured using {@link org.telegrise.telegrise.annotations.Handler @Handler} annotation.
 *
 * @see org.telegrise.telegrise.annotations.Handler
 * @since 0.1
 */
public interface UpdateHandler {
    /**
     * Determines if this handler can handle the provided update.
     * If returns <code>true</code>, method <code>handle</code> will be immediately executed.
     *
     * @param update incoming update
     * @return true if update can be handled by this handler
     */
    boolean canHandle(Update update);

    /**
     * Handles provided update after <code>canHandle</code> return <code>true</code>.
     * @param update incoming update
     */
    void handle(Update update) throws TelegramApiException;

    /**
     * Executes when method <code>handle</code> throws <code>TelegramApiException</code>.
     * @param e telegram exception
     */
    default void onException(TelegramApiException e){
        LoggerFactory.getLogger(this.getClass()).error("An exception occurred while executing handler", e);
    }
}
