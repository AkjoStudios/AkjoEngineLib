package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record FramebufferResolution(int width, int height) implements IResolution {
    @Override
    public @NotNull String toString() { return "FramebufferResolution(" + width + "x" + height + ")"; }
}