package org.telegrise.telegrise.bot.controllers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.TranscriptionManager;
import org.telegrise.telegrise.annotations.OnCreate;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.builtin.DefaultController;
import org.telegrise.telegrise.types.UserRole;

import java.util.List;

@TreeController
public class TestAdminController extends DefaultController {
    @Resource
    public SessionMemory memory;
    @Resource
    public TranscriptionManager transcriptionManager;

    private List<UserRole> roles;

    @OnCreate
    public void initialize(){
        roles = transcriptionManager.getRoles();
    }

    @Reference
    public String currentRole(){
        return memory.getUserRole().name();
    }

    @Reference
    public UserRole nextRole(Update update) {
        if (!update.hasCallbackQuery()) return null;

        int current = roles.indexOf(memory.getUserRole());
        return switch (update.getCallbackQuery().getData()){
            case "next" -> roles.get(current + 1 >= roles.size() ? 0 : current + 1);
            case "previous" -> roles.get(current - 1 < 0 ? roles.size() - 1 : current - 1);
            default -> null;
        };
    }

    @Reference
    public void setRole(UserRole role){
        memory.setUserRole(role);
    }
}
