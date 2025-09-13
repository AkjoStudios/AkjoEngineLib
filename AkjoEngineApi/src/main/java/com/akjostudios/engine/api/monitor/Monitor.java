package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.HasName;
import com.akjostudios.engine.api.common.base.area.screen.HasScreenArea;
import com.akjostudios.engine.api.common.base.position.HasPosition2D;
import com.akjostudios.engine.api.common.base.resolution.HasResolution;
import com.akjostudios.engine.api.common.base.scale.HasScale2D;
import com.akjostudios.engine.api.common.base.size.HasSize2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Monitor extends HasName, HasPosition2D, HasResolution, HasSize2D, HasScale2D, HasScreenArea {
    /**
     * @return The handle (or ID) of this monitor.
     */
    long handle();
    /**
     * @return The name of this monitor.
     */
    @NotNull String name();
    /**
     * @return The position of this monitor on the virtual screen.
     */
    @NotNull ScreenPosition position();
    /**
     * @return The resolution of this monitor.
     */
    @NotNull MonitorResolution resolution();
    /**
     * @return The refresh rate of this monitor.
     */
    int refreshRate();
    /**
     * @apiNote Might not be accurate depending on the monitor (because of incorrect EDID data or an inaccurate driver implementation).
     * @return The physical size of this monitor.
     */
    @Nullable MonitorSize size();
    /**
     * @return The content scale of this monitor scale.
     */
    @Nullable MonitorContentScale scale();
    /**
     * @return The actual work area of this monitor on the virtual screen.
     */
    @Nullable MonitorWorkArea screenArea();
    /**
     * @return The currently set gamma value of this monitor.
     */
    double gamma();
    /**
     * Sets the gamma value of this monitor to the given value.
     */
    void gamma(double gamma);
}