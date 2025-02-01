package org.telegrise.telegrise.bot.services;

import org.telegrise.telegrise.Service;

import java.time.Duration;

/**
 * This service is used to test that application and its services terminate properly upon fatal error or SIGTERM
 */
public class SleepyService implements Service {
    @Override
    public void run() {
        try {
            Thread.sleep(Duration.ofDays(1000));    // This Service is *really* tired
        } catch (InterruptedException ignored) {}
    }
}
