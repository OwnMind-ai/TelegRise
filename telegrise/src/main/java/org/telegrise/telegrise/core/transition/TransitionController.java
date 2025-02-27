package org.telegrise.telegrise.core.transition;

import lombok.Getter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.TreeExecutor;
import org.telegrise.telegrise.core.elements.Branch;
import org.telegrise.telegrise.core.elements.Transition;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.senders.UniversalSender;

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
        return switch (transition.getType()) {
            case Transition.BACK -> this.applyBack(transition, pool);
            case Transition.JUMP -> {
                this.applyJump(tree, transition, pool);
                yield false;
            }
            case Transition.CALLER -> this.applyCaller(tree, transition, pool);
            default -> throw new TelegRiseRuntimeException("Invalid type '" + transition.getType() + "'", transition.getElementNode());
        };
    }

    private boolean applyCaller(Tree tree, Transition transition, ResourcePool pool) {
        this.sessionMemory.updateJumpPoints();
        JumpPoint point = this.sessionMemory.getJumpPoints().peekLast();

        if (point == null) {
            return this.applyBack(new Transition(Transition.BACK, "root", transition.getExecute(),
                    transition.getEdit(), transition.getEditSource()), pool);
        }

        if (point.actions() != null) {
            TreeExecutor pointExecutor = this.treeExecutors.stream()
                    .filter(t -> t.getTree().getName().equals(point.from().getName()))
                    .findFirst().orElseThrow();
            ResourcePool resourcePool = new ResourcePool(pool.getUpdate(), pointExecutor.getControllerInstance(),
                    pool.getSender(), pool.getMemory(), pool.getBotUser(), pointExecutor, pool.getUpdates());

            point.actions().forEach(action -> {
                try {
                    this.sender.execute(action, resourcePool);
                } catch (TelegramApiException e) {
                    throw new TelegRiseInternalException(e);
                }
            });
        }

        if (point.nextTransition() != null) {
            if(this.treeExecutors.getLast().getTree().getName().equals(tree.getName())) {
                this.sessionMemory.getBranchingElements().removeLast();
                this.treeExecutors.getLast().beforeRemoving();
                this.treeExecutors.getLast().close();
                this.treeExecutors.removeLast();
            }

            return this.applyTransition(this.treeExecutors.getLast().getTree(), point.nextTransition(), pool);
        }

        this.applyBack(new Transition(Transition.BACK, point.from().getName(), transition.getExecute(),
                transition.getEdit(), transition.getEditSource()), pool);

        assert this.sessionMemory.getBranchingElements().getLast() instanceof Tree;

        return false;
    }

    private void applyJump(Tree tree, Transition transition, ResourcePool pool) {
        String target = transition.getTarget().generate(pool);
        BranchingElement requested = this.transcriptionMemory.get(tree, target, BranchingElement.class, List.of("tree"));
        if (requested == null) throw new TelegRiseRuntimeException("Unable to find an element called '" + target + "'", transition.getElementNode());

        this.sessionMemory.getBranchingElements().add(requested);
        this.sessionMemory.getJumpPoints().add(new JumpPoint(tree, requested, transition.getActions(), transition.getNextTransition()));
    }

    private void applyLocal(Branch branch, ExecutionOptions options, ResourcePool pool) {
        TreeExecutor last = this.treeExecutors.getLast();
        last.setCurrentBranch(branch);
        pool.getMemory().setCurrentBranch(branch);

        TreeExecutor.invokeBranch(branch.getToInvoke(), branch.getActions(), pool, last.getSender(), options);
    }

    private boolean applyBack(Transition transition, ResourcePool pool){
        if (transition.getTarget() == null){
            Branch branch = treeExecutors.getLast().getLastBranch();

            if (branch.getParent() instanceof Branch target){
                applyLocal(target, transition.getExecutionOptions(), pool);
                return true;
            } else {
                this.treeExecutors.getLast().open();
                return false;
            }
        }

        Tree lastTree = this.sessionMemory.isOnStack(Tree.class) ? (Tree) this.sessionMemory.getBranchingElements().getLast() : null;
        String name = transition.getTarget().generate(pool);
        NodeElement targetElement = this.transcriptionMemory.get(lastTree, name);

        if (targetElement == null)
            throw new TelegRiseRuntimeException("Unable to perform transition: element named '%s' doesn't exist".formatted(name), transition.getElementNode());
        if (!(targetElement instanceof BranchingElement target))
            throw new TelegRiseRuntimeException("Unable to perform transition: element named '%s' is not a tree, root or branch".formatted(name), transition.getElementNode());

        if (target instanceof Branch branch){
            applyLocal(branch, transition.getExecutionOptions(), pool);
            return true;
        } else {
            for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
                BranchingElement element = it.next();
                if (!element.getName().equals(target.getName())) {
                    this.sessionMemory.getBranchingElements().remove(element);

                    if (element instanceof Tree) {
                        assert this.treeExecutors.getLast().getTree().getName().equals(element.getName());
                        this.treeExecutors.getLast().beforeRemoving();
                        this.treeExecutors.getLast().close();
                        this.treeExecutors.removeLast();
                    }
                } else {
                    if (!this.treeExecutors.isEmpty())
                        this.treeExecutors.getLast().open();
                    return false;
                }
            }
        }

        throw new TelegRiseRuntimeException("Unable to find element called '" + target + "' in the stack", transition.getElementNode());
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
