package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.window.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record WindowState(
        @NotNull String name,
        @NotNull ScreenPosition position,
        @NotNull Monitor monitor,
        @NotNull WindowResolution resolution,
        @NotNull FramebufferResolution framebufferResolution,
        @Nullable WindowContentScale scale,
        @NotNull WindowVisibility visibility,
        @NotNull WindowOptions options,
        boolean focused,
        boolean requestedAttention
) {}