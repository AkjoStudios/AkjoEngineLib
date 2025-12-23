package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Scheduler {
    /**
     * Runs the given task after the give delay (in milliseconds) on the logic thread.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runDelayed(@NotNull Runnable task, long delayMillis);
    /**
     * Runs the given task at a fixed rate of the given frequency (in hertz) on the logic thread.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, double hertz);
    /**
     * Runs the given task after an initial delay (in milliseconds) at a fixed rate of the given frequency (in hertz) on the logic thread.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runAtFixedRate(@NotNull Runnable task, long initialDelayMillis, long periodMillis);

    /**
     * Runs the given task once on the next tick of the logic thread.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runOnceNextTick(@NotNull Runnable task);

    /**
     * Runs the given task once on the next frame of the thread associated with the given scheduler lane.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runOnceNextFrame(@NotNull Runnable task, @NotNull FrameScheduler.Lane lane);

    /**
     * Runs the given task immediately with the given scheduler lane.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable runImmediately(@NotNull Runnable task, @NotNull SchedulerLane lane);

    /**
     * @return If the given task is still scheduled.
     */
    boolean isScheduled(@NotNull Cancellable task);

    /**
     * @return An object to access the frame scheduler of the render thread.
     */
    @NotNull FrameScheduler render();

    /**
     * @return An object to access the tick scheduler of the logic thread.
     */
    @NotNull TickScheduler logic();

    /**
     * @return An object to access the frame scheduler of the audio thread.
     */
    @NotNull FrameScheduler audio();
}