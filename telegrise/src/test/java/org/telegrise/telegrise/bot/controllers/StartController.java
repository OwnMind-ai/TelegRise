package org.telegrise.telegrise.bot.controllers;

import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.TreeController;

import java.time.LocalDateTime;

@TreeController
public class StartController {
    @Reference
    public String getGreetingText(){
        var now = LocalDateTime.now().getHour();

        if (now < 12) return "Good morning!";
        if (now < 17) return "Good afternoon!";
        else return "Good evening!";
    }
}
