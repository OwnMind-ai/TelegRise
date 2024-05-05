package org.telegram.telegrise.caching;

import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Tree;

import java.util.concurrent.atomic.AtomicReference;

public class MethodReferenceCache{
    private final CachingStrategy strategy;
    private CacheContext currentContext;
    private final AtomicReference<Object> cachedValue = new AtomicReference<>(null);

    public MethodReferenceCache(CachingStrategy strategy) {
        this.strategy = strategy;
    }

    public void clear(){
        this.currentContext = null;
        this.cachedValue.set(null);
    }

    public void write(Object result, ResourcePool pool) {
        if (this.strategy == CachingStrategy.NONE) return;

        this.currentContext = this.extractContext(pool);
        if (currentContext.isValid())
            this.cachedValue.set(result);
        else
            this.clear();   // failsafe case
    }

    private CacheContext extractContext(ResourcePool pool) {
        switch (this.strategy){
            case UPDATE:
                return pool.getUpdate() != null ? new CacheContext(pool.getUpdate()) : CacheContext.INVALID;
            case TREE:
                return pool.getMemory().isOnStack(Tree.class) ? new CacheContext(pool.getMemory().getFromStack(Tree.class)) : CacheContext.INVALID;
            case BRANCH:
                return pool.getMemory().getCurrentBranch().get() != null ? new CacheContext(pool.getMemory().getCurrentBranch().get()) : CacheContext.INVALID;
            default:
                throw new IllegalStateException();
        }
    }

    public boolean isCacheApplicable(ResourcePool pool) {
        return this.strategy != CachingStrategy.NONE && this.extractContext(pool).equals(this.currentContext);
    }

    public Object getCachedValue() {
        return this.cachedValue.get();
    }
}
