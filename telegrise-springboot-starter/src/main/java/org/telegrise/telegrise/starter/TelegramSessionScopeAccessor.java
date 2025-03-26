package org.telegrise.telegrise.starter;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.TelegRiseApplication;
import org.telegrise.telegrise.core.InternalSessionExtensions;
import org.telegrise.telegrise.exceptions.TelegRiseSessionException;

@Component @RequiredArgsConstructor
public class TelegramSessionScopeAccessor {
    public final ApplicationContext context;
    public final TelegRiseApplication application;

    /**
     * Retrieves a bean from the session scope of a specified session identifier.
     *
     * @param <T> The type of the bean to retrieve.
     * @param clazz The class of the bean to retrieve.
     * @param identifier The session identifier used to locate the bean.
     * @return The bean instance of the specified type.
     * @throws NoSuchBeanDefinitionException if no bean of the specified type is found
     * @throws TelegRiseSessionException if the session does not exist
     */
    public <T> @NotNull T getBean(Class<T> clazz, SessionIdentifier identifier) {
        return ((InternalSessionExtensions) application.getSessionManager())
                .runWithSessionContext(identifier, () -> context.getBean(clazz));
    }
}
