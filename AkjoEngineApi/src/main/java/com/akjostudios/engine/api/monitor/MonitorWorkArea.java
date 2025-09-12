package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.area.screen.IScreenArea;
import com.akjostudios.engine.api.common.base.resolution.IResolution;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record MonitorWorkArea(@NotNull ScreenPosition position, @NotNull IResolution resolution) implements IScreenArea {}