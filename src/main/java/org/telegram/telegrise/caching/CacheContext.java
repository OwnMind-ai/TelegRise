package org.telegram.telegrise.caching;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.Tree;

import java.util.Objects;

public final class CacheContext {
    public static final CacheContext INVALID = new CacheContext();

    private final Integer updateId;
    private final Branch branch;
    private final String treeName;

    public CacheContext(Update update) {
        this.updateId = update.getUpdateId();
        this.branch = null;
        this.treeName = null;
    }

    public CacheContext(Branch branch) {
        this.updateId = null;
        this.branch = branch;
        this.treeName = null;
    }

    public CacheContext(Tree tree) {
        this.updateId = null;
        this.branch = null;
        this.treeName = tree.getName();
    }

    private CacheContext() {
        this.updateId = null;
        this.branch = null;
        this.treeName = null;
    }

    public boolean isValid(){
        return this.updateId != null || this.branch != null || this.treeName != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheContext that = (CacheContext) o;
        return Objects.equals(updateId, that.updateId) && Objects.equals(branch, that.branch) && Objects.equals(treeName, that.treeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateId, branch, treeName);
    }
}
