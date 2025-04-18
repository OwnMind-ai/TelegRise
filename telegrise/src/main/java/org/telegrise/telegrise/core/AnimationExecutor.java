package org.telegrise.telegrise.core;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.elements.actions.Animate;
import org.telegrise.telegrise.core.elements.actions.Frame;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.senders.BotSender;

import java.util.List;

public class AnimationExecutor implements Runnable {
    private final Animate animation;
    private final BotSender sender;
    private final ResourcePool lockedPool;
    private int messageId = -1;
    private int currentFrame = 0;

    private int loops = 0;

    public AnimationExecutor(Animate animation, BotSender sender, ResourcePool lockedPool) {
        this.animation = animation;
        this.sender = sender;
        this.lockedPool = lockedPool;
    }

    public void start(){
        if (animation.isParallel())
            new Thread(this).start();
        else
            this.run();
    }

    @Override
    public void run() {
        Frame initial = this.animation.getFrames().getFirst();
        try {
            this.executeFrame(initial, Frame.SEND, lockedPool);
            this.sleep(initial, lockedPool);
        } catch (TelegramApiException | InterruptedException e) { throw new TelegRiseInternalException(e); }
        currentFrame++;

        int maxLoops = this.animation.getLoops() != null ? this.animation.getLoops().generate(lockedPool)
                : this.animation.getUntil() != null ? Integer.MAX_VALUE : 1;

        boolean interruptedByPredicate = false;
        while(!interruptedByPredicate && maxLoops > this.loops){
            for (int i = 0; i < this.animation.getFrames().size(); i++) {
                if (this.animation.getUntil() != null && this.animation.getUntil().generate(lockedPool)){
                    interruptedByPredicate = true;
                    break;
                }

                try {
                    this.iteration(lockedPool);
                } catch (TelegramApiException | InterruptedException e) {
                    throw new TelegRiseInternalException(e);
                }
            }

            this.loops++;
        }

        if (this.animation.getAfter() != null)
            this.animation.getAfter().generate(lockedPool);

        if (this.animation.isDeleteAfter()) {
            try {
                this.sender.execute(DeleteMessage.builder().chatId(this.animation.generateChatId(lockedPool)).messageId(messageId).build());
            } catch (TelegramApiException e) {
                throw new TelegRiseInternalException(e);
            }
        }
    }

    private void iteration(ResourcePool pool) throws TelegramApiException, InterruptedException {
        Frame next = this.animation.getFrames().get(currentFrame);
        this.executeFrame(next, next.getAction(), pool);
        this.sleep(next, pool);

        currentFrame = currentFrame >= this.animation.getFrames().size() - 1 ? 0 : (currentFrame + 1);
    }

    private void sleep(Frame frame, ResourcePool pool) throws InterruptedException {
        Thread.sleep((long) ((this.animation.getPeriod().generate(pool) + frame.getDelay().generate(pool)) * 1000L));
    }

    private synchronized void executeFrame(Frame frame, String action, ResourcePool pool) throws TelegramApiException {
        switch (action){
            case Frame.SEND:
                this.messageId = this.sender.execute(SendMessage.builder()
                        .chatId(this.animation.generateChatId(pool))
                        .text(frame.getText().generateText(pool))
                        .parseMode(frame.getText().getParseMode() != null ? frame.getText().getParseMode().generate(pool) : null)
                        .entities(frame.getText().getEntities() != null ? frame.getText().getEntities().generate(pool) : List.of())
                        .build()).getMessageId();
                break;
            case Frame.EDIT:
                this.sender.execute(EditMessageText.builder()
                        .chatId(this.animation.generateChatId(pool))
                        .messageId(this.messageId)
                        .text(frame.getText().generateText(pool))
                        .parseMode(frame.getText().getParseMode() != null ? frame.getText().getParseMode().generate(pool) : null)
                        .entities(frame.getText().getEntities() != null ? frame.getText().getEntities().generate(pool) : List.of())
                        .build());
                break;
        }
    }
}
