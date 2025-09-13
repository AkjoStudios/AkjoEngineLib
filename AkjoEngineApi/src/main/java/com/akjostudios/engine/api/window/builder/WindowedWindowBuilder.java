package com.akjostudios.engine.api.window.builder;

import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.window.WindowOptions;
import com.akjostudios.engine.api.window.WindowResolution;
import com.akjostudios.engine.api.window.WindowVisibility;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowedWindowBuilder extends WindowBuilder {
    /**
     * Sets the initial resolution of the windowed window.
     */
    @NotNull WindowedWindowBuilder resolution(@NotNull WindowResolution resolution);
    /**
     * Sets the initial position of the windowed window on the virtual screen.
     */
    @NotNull WindowedWindowBuilder position(@NotNull ScreenPosition position);
    /**
     * Sets the initial position of the windowed window relative to the current monitor.
     */
    @NotNull WindowedWindowBuilder position(@NotNull MonitorPosition position);
    /**
     * Sets the initial visibility of the windowed window.
     */
    @NotNull WindowedWindowBuilder visibility(@NotNull WindowVisibility visibility);
    /**
     * Sets the initial window options for the windowed window.
     */
    @NotNull WindowedWindowBuilder options(@NotNull WindowOptions options);
}