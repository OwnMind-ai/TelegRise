package org.telegram.telegrise.caching;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.elements.Branch;
import org.telegram.telegrise.core.elements.Tree;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Getter
public final class CacheContext {
    public static final CacheContext INVALID = new CacheContext();

    private final Integer updateId;
    private final Branch branch;
    private final Tree tree;

    public CacheContext(Update update) {
        this.updateId = update.getUpdateId();
        this.branch = null;
        this.tree = null;
    }

    public CacheContext(Branch branch) {
        this.updateId = null;
        this.branch = branch;
        this.tree = null;
    }

    public CacheContext(Tree tree) {
        this.updateId = null;
        this.branch = null;
        this.tree = tree;
    }

    private CacheContext() {
        this.updateId = null;
        this.branch = null;
        this.tree = null;
    }

    public boolean isValid(){
        return this.updateId != null || this.branch != null || this.tree != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheContext that = (CacheContext) o;
        return Objects.equals(updateId, that.updateId) && Objects.equals(branch, that.branch) && Objects.equals(tree.getName(), that.tree.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateId, branch, tree);
    }

    public boolean applicable(CacheContext currentContext) {
        if (currentContext == null) return false;
        if (updateId != null) return this.equals(currentContext);

        if (branch != null && currentContext.getBranch() != null)
            return traverseBranchingElement(currentContext.getBranch(), branch, Branch::getBranches);
        if (tree != null && currentContext.getTree() != null)
            return tree == currentContext.getTree();
        if (tree != null && currentContext.getBranch() != null)
            return tree.getBranches().stream().anyMatch(b -> traverseBranchingElement(currentContext.getBranch(), b, Branch::getBranches));

        return false;
    }

    private <T> boolean traverseBranchingElement(T target, T compareTo, Function<T, List<T>> nextFunction){
        if (target == compareTo) return true;

        List<T> next = nextFunction.apply(compareTo);
        if (next == null) return false;

        for(T t : next)
            return traverseBranchingElement(target, t, nextFunction);

        return false;
    }
}
