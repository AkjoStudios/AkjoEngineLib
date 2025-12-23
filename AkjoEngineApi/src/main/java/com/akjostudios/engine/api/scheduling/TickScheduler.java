package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface TickScheduler {
    /**
     * Runs the given task on every tick of this scheduler.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable everyTick(@NotNull Runnable task);

    /**
     * Runs the given task after the given tick amount once.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable afterTicks(int ticks, @NotNull Runnable task);

    /**
     * Runs the given task as soon as possible.
     * @return An object which can be used to cancel the task.
     */
    default @NotNull Cancellable immediate(@NotNull Runnable task) {
        return afterTicks(0, task);
    }

    /**
     * @return The current tick count
     */
    long currentTick();
}