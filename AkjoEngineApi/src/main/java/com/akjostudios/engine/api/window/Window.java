package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.HasName;
import com.akjostudios.engine.api.common.base.area.screen.HasScreenArea;
import com.akjostudios.engine.api.common.base.position.HasPosition2D;
import com.akjostudios.engine.api.common.base.resolution.HasResolution;
import com.akjostudios.engine.api.common.base.scale.HasScale2D;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Window extends HasName, HasPosition2D, HasResolution, HasScale2D, HasScreenArea {
    /**
     * @return The handle (or ID) of this window.
     */
    long handle();
    /**
     * @return The name/title of this window.
     */
    @Override String name();
    /**
     * Sets the name/title of this window.
     */
    void name(@NotNull String name);
    /**
     * @return The position of this window on the virtual screen.
     */
    @NotNull ScreenPosition position();
    /**
     * Sets the position of this window on the virtual screen.
     */
    void position(@NotNull ScreenPosition position);
    /**
     * @return The position of this window relative to the monitor.
     */
    @NotNull MonitorPosition monitorPosition();
    /**
     * Sets the position of this window relative to the current monitor.
     */
    void monitorPosition(@NotNull MonitorPosition position);
    /**
     * @return The current monitor this window is on.
     */
    @NotNull Monitor monitor();
    /**
     * @return The current resolution of this window.
     */
    @NotNull WindowResolution resolution();
    /**
     * Sets the resolution of this window to the given value.
     */
    void resolution(@NotNull WindowResolution resolution);
    /**
     * @return The current scale of this window.
     */
    @NotNull WindowContentScale scale();
    /**
     * @return The actual work area of this window on the virtual screen.
     */
    default @NotNull WindowWorkArea screenArea() {
        return new WindowWorkArea(position(), resolution());
    }
    /**
     * @return The actual work area of this window relative to the monitor.
     */
    default @NotNull RelativeWindowWorkArea relativeScreenArea() {
        return new RelativeWindowWorkArea(monitorPosition(), resolution());
    }
    /**
     * @return The current visibility state of this window.
     */
    @NotNull WindowVisibility visibility();
    /**
     * Sets the visibility state of this window to the given value.
     */
    void visibility(@NotNull WindowVisibility visibility);
    /**
     * @return The current options set for this window.
     */
    @NotNull WindowOptions options();
    /**
     * Sets the options for this window to the given value.
     */
    void options(@NotNull WindowOptions options);
}