package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

public interface TickScheduler {
    @NotNull Cancellable everyTick(@NotNull Runnable task);
    @NotNull Cancellable afterTicks(int ticks, @NotNull Runnable task);
    long currentTick();
}