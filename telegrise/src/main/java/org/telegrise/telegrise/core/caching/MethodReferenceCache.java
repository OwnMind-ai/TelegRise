package org.telegrise.telegrise.core.caching;

import lombok.Getter;
import org.telegrise.telegrise.caching.CachingStrategy;
import org.telegrise.telegrise.core.ResourcePool;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class contains cached values and context
 * in which they can be used for a specific {@link MethodReferenceCache method reference}.
 * Cache can only be used if reference's caching strategy is other than {@code NONE}.
 *
 * @see CacheContext
 * @since 0.6
 */
public class MethodReferenceCache{
    @Getter
    private final CachingStrategy strategy;
    @Getter
    private CacheContext currentContext;
    private final AtomicReference<Object> cachedValue = new AtomicReference<>(null);

    public MethodReferenceCache(CachingStrategy strategy) {
        this.strategy = strategy;
    }

    public void clear(){
        this.currentContext = null;
        this.cachedValue.set(null);
    }

    public boolean isEmpty(){
        return currentContext == null;
    }

    public void write(Object result, ResourcePool pool) {
        if (this.strategy == CachingStrategy.NONE) return;

        this.currentContext = this.extractMethodContext(pool);
        if (currentContext.isValid())
            this.cachedValue.set(result);
        else
            this.clear();   // failsafe case
    }

    private CacheContext extractMethodContext(ResourcePool pool) {
        //noinspection SwitchStatementWithTooFewBranches; There will be more in the future
        return switch (this.strategy) {
            case UPDATE -> pool.getUpdate() != null ? new CacheContext(pool.getUpdate()) : CacheContext.INVALID;
            default -> throw new IllegalStateException();
        };
    }

    public boolean isCacheApplicable(ResourcePool pool) {
        return this.strategy != CachingStrategy.NONE && this.currentContext != null
                && this.currentContext.applicable(this.extractCheckContext(pool));
    }

    private CacheContext extractCheckContext(ResourcePool pool) {
        if (pool.getUpdate() != null)
            return new CacheContext(pool.getUpdate());
        else
            return null;
    }

    public Object getCachedValue() {
        return this.cachedValue.get();
    }
}
