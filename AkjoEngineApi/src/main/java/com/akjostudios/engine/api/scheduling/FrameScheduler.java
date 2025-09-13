package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface FrameScheduler {
    /**
     * Runs the given task on every frame of this scheduler.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable everyFrame(@NotNull Runnable task);
    /**
     * Runs the given task after the given frame amount once.
     * @return An object which can be used to cancel the task.
     */
    @NotNull Cancellable afterFrames(int frames, @NotNull Runnable task);
    /**
     * @return The current frame count
     */
    long currentFrame();

    /**
     * Adds the post-frame task for this frame scheduler.
     * @apiNote Must be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    void __engine_addPostFrameTask(
            @NotNull Object token,
            @NotNull Runnable task
    ) throws IllegalCallerException, IllegalStateException;
}