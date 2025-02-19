package org.telegrise.telegrise.bot.controllers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.builtin.DefaultController;

@TreeController
public class TestKeyboardController extends DefaultController {
    @Resource
    private SessionMemory memory;

    @Reference
    private int button1, button2;

    @Reference
    public void incrementButton(Update update){
        if (update.getCallbackQuery().getData().endsWith("1"))
            button1++;
        else
            button2++;
    }

    @Reference
    public void disableButton(){
        memory.getKeyboardState("MainKeyboard").disableButtonsOfData("disable");
    }
}
