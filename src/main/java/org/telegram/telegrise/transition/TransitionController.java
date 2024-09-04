package org.telegram.telegrise.transition;

import lombok.Getter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.TreeExecutor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.senders.BotSender;
import org.telegram.telegrise.senders.UniversalSender;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class TransitionController {
    private final SessionMemoryImpl sessionMemory;
    @Getter
    private final Deque<TreeExecutor> treeExecutors;
    private final TranscriptionMemory transcriptionMemory;
    private final UniversalSender sender;


    public TransitionController(SessionMemoryImpl sessionMemory, Deque<TreeExecutor> treeExecutors, TranscriptionMemory transcriptionMemory, BotSender sender) {
        this.sessionMemory = sessionMemory;
        this.treeExecutors = treeExecutors;
        this.transcriptionMemory = transcriptionMemory;
        this.sender = new UniversalSender(sender);
    }

    public boolean applyTransition(Tree tree, Transition transition, ResourcePool pool){

        return switch (transition.getDirection()) {
            case Transition.PREVIOUS -> {
                this.applyPrevious(transition, pool);
                yield false;
            }
            case Transition.JUMP -> {
                this.applyJump(tree, transition, pool);
                yield false;
            }
            case Transition.LOCAL -> {
                this.applyLocal(tree, transition, pool);
                yield true; // INTERRUPTING
            }
            case Transition.CALLER -> this.applyCaller(tree, transition, pool);
            default -> throw new TelegRiseRuntimeException("Invalid direction '" + transition.getDirection() + "'", transition.getElementNode());
        };
    }

    private boolean applyCaller(Tree tree, Transition transition, ResourcePool pool) {
        this.sessionMemory.updateJumpPoints();
        JumpPoint point = this.sessionMemory.getJumpPoints().peekLast();

        if (point == null)
            throw new TelegRiseRuntimeException("Unable to find a caller of tree '" + tree.getName() + "'", transition.getElementNode());

        if (point.getActions() != null) {
            TreeExecutor pointExecutor = this.treeExecutors.stream()
                    .filter(t -> t.getTree().getName().equals(point.getFrom().getName()))
                    .findFirst().orElseThrow();
            ResourcePool resourcePool = new ResourcePool(pool.getUpdate(), pointExecutor.getControllerInstance(),
                    pool.getSender(), pool.getMemory(), pointExecutor, pool.getUpdates());

            point.getActions().forEach(action -> {
                try {
                    this.sender.execute(action, resourcePool);
                } catch (TelegramApiException e) {
                    throw new TelegRiseInternalException(e);
                }
            });
        }

        if (point.getNextTransition() != null) {
            if(this.treeExecutors.getLast().getTree().getName().equals(tree.getName())) {
                this.sessionMemory.getBranchingElements().removeLast();
                this.treeExecutors.getLast().beforeRemoving();
                this.treeExecutors.getLast().close();
                this.treeExecutors.removeLast();
            }

            return this.applyTransition(this.treeExecutors.getLast().getTree(), point.getNextTransition(), pool);
        }

        this.applyPrevious(new Transition(Transition.PREVIOUS, GeneratedValue.ofValue(point.getFrom().getName()), null, false, null, null), pool);

        assert this.sessionMemory.getBranchingElements().getLast() instanceof Tree;
        if (transition.getType().equals(Transition.MENU_TYPE)){
            for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
                if (it.next() instanceof Root) break;
                this.sessionMemory.getBranchingElements().removeLast();
            }
        }

        return false;
    }

    private void applyLocal(Tree tree, Transition transition, ResourcePool pool) {
        TreeExecutor last = this.treeExecutors.getLast();
        assert last.getTree().getName().equals(tree.getName());

        Branch next = this.transcriptionMemory.get(last.getTree(), transition.getTarget().generate(pool), Branch.class, List.of("branch"));
        last.setCurrentBranch(next);

        if (transition.isExecute())
            TreeExecutor.invokeBranch(next.getToInvoke(), next.getActions(), pool,  last.getSender());
    }

    private void applyJump(Tree tree, Transition transition, ResourcePool pool) {
        String target = transition.getTarget().generate(pool);
        BranchingElement requested = this.transcriptionMemory.get(tree, target, BranchingElement.class, Transition.TYPE_LIST);
        if (requested == null) throw new TelegRiseRuntimeException("Unable to find an element called '" + target + "'", transition.getElementNode());

        this.sessionMemory.getBranchingElements().add(requested);
        this.sessionMemory.getJumpPoints().add(new JumpPoint(tree, requested, transition.getActions(), transition.getNextTransition()));
    }

    private void applyPrevious(Transition transition, ResourcePool pool){
        String target = transition.getTarget().generate(pool);

        for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
            BranchingElement element = it.next();

            if (!element.getName().equals(target)) {
                this.sessionMemory.getBranchingElements().remove(element);

                if (element instanceof Tree){
                    assert this.treeExecutors.getLast().getTree().getName().equals(element.getName());
                    this.treeExecutors.getLast().beforeRemoving();
                    this.treeExecutors.getLast().close();
                    this.treeExecutors.removeLast();
                }
            } else {
                if (!this.treeExecutors.isEmpty())
                    this.treeExecutors.getLast().open();
                return;
            }
        }

        throw new TelegRiseRuntimeException("Unable to find element called '" + target + "'", transition.getElementNode());
    }

    public void removeExecutor(TreeExecutor executor){
        executor.close();
        executor.beforeRemoving();
        this.treeExecutors.remove(executor);
        this.sessionMemory.setCurrentBranch(null);

        assert this.sessionMemory.getBranchingElements().getLast().equals(executor.getTree());
        this.sessionMemory.getBranchingElements().removeLast();
    }
}
