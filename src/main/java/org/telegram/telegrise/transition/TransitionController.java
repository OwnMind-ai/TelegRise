package org.telegram.telegrise.transition;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.TreeExecutor;
import org.telegram.telegrise.UniversalSender;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.parser.TranscriptionMemory;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class TransitionController {
    public final SessionMemoryImpl sessionMemory;
    public final Deque<TreeExecutor> treeExecutors;
    public final TranscriptionMemory transcriptionMemory;
    private final UniversalSender sender;


    public TransitionController(SessionMemoryImpl sessionMemory, Deque<TreeExecutor> treeExecutors, TranscriptionMemory transcriptionMemory, DefaultAbsSender sender) {
        this.sessionMemory = sessionMemory;
        this.treeExecutors = treeExecutors;
        this.transcriptionMemory = transcriptionMemory;
        this.sender = new UniversalSender(sender);
    }

    public boolean applyTransition(Tree tree, Transition transition, ResourcePool pool){
        switch (transition.getDirection()){
            case Transition.NEXT: this.applyNext(tree, transition); return false;
            case Transition.PREVIOUS: this.applyPrevious(transition); return false;
            case Transition.JUMP: this.applyJump(tree, transition); return false;
            case Transition.LOCAL: this.applyLocal(tree, transition, pool); return true;   // INTERRUPTING
            case Transition.CALLER: return this.applyCaller(tree, transition, pool);
            default: throw new TelegRiseRuntimeException("Invalid direction '" + transition.getDirection() + "'");
        }
    }

    private boolean applyCaller(Tree tree, Transition transition, ResourcePool pool) {
        this.sessionMemory.updateJumpPoints();
        JumpPoint point = this.sessionMemory.getJumpPoints().peekLast();

        if (point == null)
            throw new TelegRiseRuntimeException("Unable to find a caller of tree '" + tree.getName() + "'");

        if (point.getActions() != null) {
            TreeExecutor pointExecutor = this.treeExecutors.stream()
                    .filter(t -> t.getTree().getName().equals(point.getFrom().getName()))
                    .findFirst().orElseThrow();
            ResourcePool resourcePool = new ResourcePool(pool.getUpdate(), pointExecutor.getControllerInstance(),
                    pool.getSender(), pool.getMemory(), pointExecutor);

            point.getActions().forEach(action -> {
                try {
                    this.sender.execute(action, resourcePool);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (point.getNextTransition() != null) {
            if(this.treeExecutors.getLast().getTree().getName().equals(tree.getName())) {
                this.treeExecutors.getLast().close();
                this.treeExecutors.removeLast();
            }

            return this.applyTransition(this.treeExecutors.getLast().getTree(), point.getNextTransition(), pool);
        }

        this.applyPrevious(new Transition(Transition.PREVIOUS, point.getFrom().getName(), null, false, null, null));

        assert this.sessionMemory.getBranchingElements().getLast() instanceof Tree;
        if (transition.getType().equals(Transition.MENU_TYPE)){
            for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
                if (it.next() instanceof Menu) break;
                this.sessionMemory.getBranchingElements().removeLast();
            }
        }

        return false;
    }

    private void applyLocal(Tree tree, Transition transition, ResourcePool pool) {
        TreeExecutor last = this.treeExecutors.getLast();
        assert last.getTree().getName().equals(tree.getName());

        Branch next = this.transcriptionMemory.get(transition.getTarget(), Branch.class, List.of("branch"));
        last.setCurrentBranch(next);

        if (transition.isExecute())
            TreeExecutor.invokeBranch(next.getToInvoke(), next.getActions(), pool,  last.getSender());
    }

    private void applyJump(Tree tree, Transition transition) {
        BranchingElement requested = this.transcriptionMemory.get(transition.getTarget(), BranchingElement.class, Transition.TYPE_LIST);
        if (requested == null) throw new TelegRiseRuntimeException("Unable to find an element called '" + transition.getTarget() + "'");

        this.sessionMemory.getBranchingElements().add(requested);
        this.sessionMemory.getJumpPoints().add(new JumpPoint(tree, requested, transition.getActions(), transition.getNextTransition()));
    }

    private void applyNext(Tree tree, Transition transition) {
        Menu next = tree.getMenus().stream().filter(m -> m.getName().equals(transition.getTarget())).findFirst()
                .orElseThrow(() -> new TelegRiseRuntimeException("Unable to find a menu '" + transition.getTarget() + "' in tree '" + tree.getName() + "'"));

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

        this.treeExecutors.getLast().open();

        throw new TelegRiseRuntimeException("Unable to find element called '" + transition.getTarget() + "'");
    }

    public void removeExecutor(TreeExecutor executor){
        executor.close();
        executor.beforeRemoving();
        this.treeExecutors.remove(executor);
        this.sessionMemory.getCurrentBranch().set(null);

        assert this.sessionMemory.getBranchingElements().getLast().equals(executor.getTree());
        this.sessionMemory.getBranchingElements().removeLast();
    }
}
