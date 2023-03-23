package org.telegram.telegrise;

import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.Menu;
import org.telegram.telegrise.core.elements.Transition;
import org.telegram.telegrise.core.elements.Tree;

import java.util.Deque;
import java.util.Iterator;

public class TransitionController {
    public final SessionMemoryImpl sessionMemory;
    public final Deque<TreeExecutor> treeExecutors;

    public TransitionController(SessionMemoryImpl sessionMemory, Deque<TreeExecutor> treeExecutors) {
        this.sessionMemory = sessionMemory;
        this.treeExecutors = treeExecutors;
    }

    public void applyTransition(Tree tree, Transition transition){
        switch (transition.getDirection()){
            case Transition.NEXT: this.applyNext(tree, transition); break;
            case Transition.PREVIOUS: this.applyPrevious(transition); break;
            case Transition.JUMP: this.applyJump(tree, transition); break;
            case Transition.LOCAL: this.applyLocal(tree, transition); break;
            default: throw new TelegRiseRuntimeException("Invalid direction '" + transition.getDirection() + "'");
        }
    }

    private void applyLocal(Tree tree, Transition transition) {
    }

    private void applyJump(Tree tree, Transition transition) {
    }

    private void applyNext(Tree tree, Transition transition) {
        Menu next = tree.getMenus().stream().filter(m -> m.getName().equals(transition.getTarget())).findFirst()
                .orElseThrow(() -> new TelegRiseRuntimeException("Unable to find menu '" + transition.getTarget() + "' in tree '" + tree.getName() + "'"));

        this.sessionMemory.getBranchingElements().add(next);
    }

    private void applyPrevious(Transition transition){
        for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
            BranchingElement element = it.next();

            if (!element.getName().equals(transition.getTarget())) {
                this.sessionMemory.getBranchingElements().remove(element);

                if (element instanceof Tree){
                    assert this.treeExecutors.getLast().getTree().getName().equals(element.getName());
                    this.treeExecutors.getLast().close();
                    this.treeExecutors.removeLast();
                }
            } else
                return;
        }

        throw new TelegRiseRuntimeException("Unable to find element called '" + transition.getTarget() + "'");
    }

    public void removeExecutor(TreeExecutor executor){
        this.treeExecutors.remove(executor);
        this.sessionMemory.getCurrentBranch().set(null);

        assert this.sessionMemory.getBranchingElements().getLast().equals(executor.getTree());
        this.sessionMemory.getBranchingElements().removeLast();
    }
}
