package org.telegrise.telegrise.core.caching;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

/**
 * This class describes the context in which a {@link MethodReferenceCache reference cache} can be applied.
 *
 * @since 0.6
 */
@Getter
public final class CacheContext {
    public static final CacheContext INVALID = new CacheContext();

    private final Integer updateId;

    public CacheContext(Update update) {
        this.updateId = update.getUpdateId();
    }

    private CacheContext() {
        this.updateId = null;
    }

    public boolean isValid(){
        return this.updateId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheContext that = (CacheContext) o;
        return Objects.equals(updateId, that.updateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateId);
    }

    /**
     * Determines if this cache's context can be applied to current context.
     *
     * @param currentContext current context
     * @return true, if cache can be applied
     */
    public boolean applicable(CacheContext currentContext) {
        if (currentContext == null) return false;
        if (updateId != null) return this.equals(currentContext);

        return false;
    }
}
