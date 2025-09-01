package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Scheduler {
    @NotNull Cancellable runDelayed(@NotNull Runnable task, long delayMillis);
    @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, long initialDelayMillis, long periodMillis);
    @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, double hertz);
    @NotNull Cancellable runOnceNextTick(@NotNull Runnable task);
    @NotNull Cancellable runOnceNextFrame(@NotNull Runnable task);
    boolean isScheduled(@NotNull Cancellable task);
}