package org.telegrise.telegrise.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.SessionInitializer;
import org.telegrise.telegrise.SessionMemory;

import java.util.List;

@Slf4j
public class Initializer implements SessionInitializer {
    private final Long adminId;

    public Initializer(Long adminId) {
        this.adminId = adminId;
    }

    @Override
    public void initialize(SessionMemory memory) {
        log.info("User session initialized {}", memory.getSessionIdentifier());
    }

    @Override
    public List<SessionIdentifier> getInitializionList() {
        return List.of(SessionIdentifier.ofUserOnly(adminId));
    }
}
