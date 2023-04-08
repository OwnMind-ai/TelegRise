package org.telegram.telegrise.types;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public final class CommandData {
    private final String name;
    private final @Nullable String username;
}
