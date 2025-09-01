package com.akjostudios.engine.api.scheduling;

import com.akjostudios.engine.api.common.cancel.Cancellable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface FrameScheduler {
    @NotNull Cancellable everyFrame(@NotNull Runnable task);
    @NotNull Cancellable afterFrames(int frames, @NotNull Runnable task);
    long currentFrame(@NotNull RenderLane lane);
}