package org.telegrise.telegrise.starter;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.core.TelegRiseSessionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-based scope for TelegRise application.
 * <p>
 * This scope provides built-in resources such as {@code SessionMemory} and other, as well as storing user's beans in scope of {@code "telegramSession"}.
 * @since 1.0
 */
public class TelegRiseSessionScope implements Scope {
    public static final String NAME = "telegramSession";

    private final Map<SessionIdentifier, Map<String, Object>> beans = new ConcurrentHashMap<>();
    private final Map<SessionIdentifier, Map<String, Runnable>> destructionCallbacks = new ConcurrentHashMap<>();

    @Override
    public @NotNull Object get(@NotNull String name, @NotNull ObjectFactory<?> objectFactory) {
        TelegRiseSessionContext context = TelegRiseSessionContext.getCurrentContext();
        if (context == null) return objectFactory.getObject();

        var contextBean = context.getInjector().get(name);
        if (contextBean != null) return contextBean;

        return get(beans).computeIfAbsent(name, key -> objectFactory.getObject());
    }

    @Override
    public Object remove(@NotNull String name) {
        get(destructionCallbacks).remove(name);
        return get(beans).remove(name);
    }

    @Override
    public void registerDestructionCallback(@NotNull String name, @NotNull Runnable callback) {
        get(destructionCallbacks).put(name, callback);
    }

    @Override
    public Object resolveContextualObject(@NotNull String key) {
        TelegRiseSessionContext context = TelegRiseSessionContext.getCurrentContext();
        if (context == null) return null;

        if (key.equals("memory"))
            return context.getInjector().get(SessionMemory.class);

        return null;
    }

    @Override
    public String getConversationId() {
        return TelegRiseSessionContext.getCurrentContext().getInjector()
                .get(SessionMemory.class).getSessionIdentifier().toString();
    }

    public void destroySession(SessionIdentifier identifier){
        if (!beans.containsKey(identifier)) return;

        beans.remove(identifier);
        destructionCallbacks.get(identifier).values().forEach(Runnable::run);
        destructionCallbacks.remove(identifier);
    }

    private <T> Map<String, T> get(Map<SessionIdentifier, Map<String, T>> map){
        TelegRiseSessionContext context = TelegRiseSessionContext.getCurrentContext();
        if (context == null)
            throw new NoSuchBeanDefinitionException("Telegram-session-scoped beans were accessed outside the session's thread");

        return map.computeIfAbsent(context.getIdentifier(), k -> new HashMap<>());
    }
}