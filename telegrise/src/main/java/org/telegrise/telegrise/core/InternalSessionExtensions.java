package org.telegrise.telegrise.core;

import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.SessionIdentifier;

import java.util.function.Supplier;

@ApiStatus.Internal
public interface InternalSessionExtensions {
    @ApiStatus.Internal
    <T> T runWithSessionContext(SessionIdentifier identifier, Supplier<T> runnable);
}
