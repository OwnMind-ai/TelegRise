package org.telegrise.telegrise.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegrise.telegrise.TelegRiseApplication;
import org.telegrise.telegrise.bot.services.InfoService;
import org.telegrise.telegrise.bot.services.SleepyService;

import java.io.File;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Starting test application");

        Long adminId = System.getenv("ADMIN_ID") != null ? Long.parseLong(System.getenv("ADMIN_ID")) : null;
        if (adminId == null)
            log.warn("Admin id wasn't found. Please, specify 'ADMIN_ID' environment variable with a valid TelegramID");

        TelegRiseApplication application = new TelegRiseApplication(new File("telegrise/src/test/resources/bot/index.xml"), Main.class);
        application.setSessionInitializer(new Initializer(adminId));
        application.setRoleProvider((memory) -> {
            var role = adminId != null && memory.getUserId() == adminId ? "admin" : "user";
            log.info("User {} got assigned {} role", memory.getUserId(), role);
            return role;
        });

        application.addService(new SleepyService());
        application.addService(new InfoService());

        application.start();
    }
}