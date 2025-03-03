package org.telegrise.telegrise.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionMemory;

/**
 * This class acts as a relay between application session resources and {@code TelegRiseSessionScope} in Spring Boot starter module.
 * Use of this class in any way outside the {@code org.telegrise} package is highly prohibited and may cause unexpected behavior or errors.
 * <p>
 * Context must be created at the beginning of the session lifecycle (after ExecutorService assigned a thread for it)
 * and destroyed right after the end of execution. Otherwise, contexts of different sessions may mix, causing mayhem and total chaos.
 *
 * @since 1.0
 */
@ApiStatus.Internal
@Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegRiseSessionContext {
    private final static ThreadLocal<TelegRiseSessionContext> currentContext = new ThreadLocal<>();

    private final SessionIdentifier identifier;
    private final SessionMemory memory;
    private final ResourceInjector injector;

    public static void setCurrentContext(SessionIdentifier identifier, SessionMemory memory, ResourceInjector injector){
        currentContext.set(new TelegRiseSessionContext(identifier, memory, injector));
    }

    public static void clearContext(){
        currentContext.remove();
    }

    public static TelegRiseSessionContext getCurrentContext(){
        return currentContext.get();
    }
}
