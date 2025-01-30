package org.telegrise.telegrise.types;

import org.jetbrains.annotations.Nullable;

public record CommandData(String name, @Nullable String username) {
}
