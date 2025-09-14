package com.akjostudios.engine.runtime.impl.monitor;

import com.akjostudios.engine.api.monitor.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MonitorState(
        @NotNull String name,
        @NotNull ScreenPosition position,
        @NotNull MonitorResolution resolution,
        int refreshRate,
        @Nullable MonitorSize size,
        @Nullable MonitorContentScale scale,
        @Nullable MonitorWorkArea workArea,
        double gamma
) {}