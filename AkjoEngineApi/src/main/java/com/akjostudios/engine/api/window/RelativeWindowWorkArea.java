package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.area.screen.IScreenArea;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record RelativeWindowWorkArea(@NotNull MonitorPosition position, @NotNull WindowResolution resolution) implements IScreenArea {
    @Override
    public @NotNull String toString() {
        return "RelativeWindowWorkArea(" + position + ", " + resolution + ")";
    }
}