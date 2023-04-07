package org.telegram.telegrise.transition;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.TreeExecutor;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.*;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.types.DynamicKeyboard;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class TransitionController {
    public final SessionMemoryImpl sessionMemory;
    public final Deque<TreeExecutor> treeExecutors;
    public final TranscriptionMemory transcriptionMemory;

    public TransitionController(SessionMemoryImpl sessionMemory, Deque<TreeExecutor> treeExecutors, TranscriptionMemory transcriptionMemory) {
        this.sessionMemory = sessionMemory;
        this.treeExecutors = treeExecutors;
        this.transcriptionMemory = transcriptionMemory;
    }

    public boolean applyTransition(Tree tree, Transition transition, ResourcePool pool){
        switch (transition.getDirection()){
            case Transition.NEXT: this.applyNext(tree, transition); return false;
            case Transition.PREVIOUS: this.applyPrevious(transition); return false;
            case Transition.JUMP: this.applyJump(tree, transition); return false;
            case Transition.LOCAL: this.applyLocal(tree, transition, pool); return true;   // INTERRUPTING
            case Transition.CALLER: this.applyCaller(tree, transition); return false;
            default: throw new TelegRiseRuntimeException("Invalid direction '" + transition.getDirection() + "'");
        }
    }

    public void applyUpdate(Tree tree, Refresh refresh, ResourcePool pool) {
        Message targetMessage = this.extractMessageTarget(refresh, pool);

        try {
            this.refreshMessage(refresh, targetMessage, pool);
        } catch (TelegramApiException e) {
            if (!refresh.isSneaky()) throw new RuntimeException(e);
        }

        if (refresh.isTransit()) {
            assert tree.getName().equals(this.treeExecutors.getLast().getTree().getName());
            this.treeExecutors.getLast().setCurrentBranch(null);
        }
    }

    private void refreshMessage(Refresh refresh, Message target, ResourcePool pool) throws TelegramApiException {
        DefaultAbsSender sender = pool.getSender();
        InlineKeyboardMarkup markup = refresh.getKeyboardId() == null ? null :
                pool.getMemory().get(refresh.getKeyboardId(), DynamicKeyboard.class).createInline(pool);

        if (refresh.getText() == null && markup != null){
            sender.execute(EditMessageReplyMarkup.builder()
                            .chatId(target.getChatId())
                            .messageId(target.getMessageId())
                            .replyMarkup(markup)
                            .build());
        } else if (refresh.getText() != null){
            Text text = refresh.getText();

            if (MessageUtils.hasMedia(target))
                sender.execute(EditMessageCaption.builder()
                        .chatId(target.getChatId())
                        .messageId(target.getMessageId())
                        .caption(text.getText().generate(pool))
                        .captionEntities(text.getEntities() != null ? text.getEntities().generate(pool) : List.of())
                        .parseMode(text.getParseMode() != null ? text.getParseMode().generate(pool) : null)
                        .replyMarkup(markup)
                        .build());
            else
                sender.execute(EditMessageText.builder()
                        .chatId(target.getChatId())
                        .messageId(target.getMessageId())
                        .text(text.getText().generate(pool))
                        .entities(text.getEntities() != null ? text.getEntities().generate(pool) : List.of())
                        .parseMode(text.getParseMode() != null ? text.getParseMode().generate(pool) : null)
                        .replyMarkup(markup)
                        .build());
        } else
            throw new TelegRiseRuntimeException("Nothing to refresh");
    }

    private Message extractMessageTarget(Refresh refresh, ResourcePool pool){
        if (Refresh.LAST.equals(refresh.getType())){
            if (pool.getMemory().getLastSentMessage() == null)
                throw new TelegRiseRuntimeException("Unable to apply refresh element: last sent message doesn't exists");
            
            return pool.getMemory().getLastSentMessage();
        } else if (Refresh.CALLBACK.equals(refresh.getType())) {
            if (pool.getUpdate() == null || !pool.getUpdate().hasCallbackQuery())
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed update has no callback query");

            return pool.getUpdate().getCallbackQuery().getMessage();
        }

        throw new TelegRiseRuntimeException("Unable to apply refresh element: unknown refresh type " + refresh.getType());
    }

    private void applyCaller(Tree tree, Transition transition) {
        this.sessionMemory.updateJumpPoints();
        JumpPoint point = this.sessionMemory.getJumpPoints().peekLast();

        if (point == null)
            throw new TelegRiseRuntimeException("Unable to find a caller of tree '" + tree.getName() + "'");

        this.applyPrevious(new Transition(Transition.PREVIOUS, point.getFrom().getName(), null, false));

        assert this.sessionMemory.getBranchingElements().getLast() instanceof Tree;
        if (transition.getType().equals(Transition.MENU_TYPE)){
            for (Iterator<BranchingElement> it = this.sessionMemory.getBranchingElements().descendingIterator(); it.hasNext(); ) {
                if (it.next() instanceof Menu) return;
                this.sessionMemory.getBranchingElements().removeLast();
            }
        }
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
        this.sessionMemory.getJumpPoints().add(new JumpPoint(tree, requested));
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

        throw new TelegRiseRuntimeException("Unable to find element called '" + transition.getTarget() + "'");
    }

    public void removeExecutor(TreeExecutor executor){
        executor.close();
        this.treeExecutors.remove(executor);
        this.sessionMemory.getCurrentBranch().set(null);

        assert this.sessionMemory.getBranchingElements().getLast().equals(executor.getTree());
        this.sessionMemory.getBranchingElements().removeLast();
    }
}
