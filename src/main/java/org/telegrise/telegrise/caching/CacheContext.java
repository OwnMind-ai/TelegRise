package org.telegrise.telegrise.caching;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

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

    public boolean applicable(CacheContext currentContext) {
        if (currentContext == null) return false;
        if (updateId != null) return this.equals(currentContext);

        return false;
    }
}
