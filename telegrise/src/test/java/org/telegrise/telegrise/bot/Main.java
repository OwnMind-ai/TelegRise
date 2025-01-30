package org.telegrise.telegrise.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegrise.telegrise.TelegRiseApplication;

import java.io.File;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Starting test application");

        Long adminId = System.getenv("ADMIN_ID") != null ? Long.parseLong(System.getenv("ADMIN_ID")) : null;
        if (adminId == null)
            log.warn("Admin id wasn't found. Please, specify 'ADMIN_ID' environment variable with a valid TelegramID");

        TelegRiseApplication application = new TelegRiseApplication(new File("telegrise/src/test/resources/bot/index.xml"), Main.class);
        application.setRoleProvider((user, sessionMemory) -> user.getId().equals(adminId) ? "admin" : "user");

        application.start();
    }
}
