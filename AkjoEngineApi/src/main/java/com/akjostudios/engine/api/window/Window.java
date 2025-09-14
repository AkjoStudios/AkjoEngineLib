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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Window extends HasName, HasPosition2D, HasResolution, HasScale2D, HasScreenArea {
    /**
     * @return The handle (or ID) of this window.
     */
    long handle();
    /**
     * @return The name/title of this window.
     */
    @NotNull String name();
    /**
     * @return The name/title of this window.
     */
    default @NotNull String title() { return name(); }
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
     * @throws IllegalArgumentException When the given position is outside the range of an integer.
     */
    void position(@NotNull ScreenPosition position) throws IllegalArgumentException;
    /**
     * @throws IllegalStateException When the window is not attached to a monitor.
     * @return The position of this window relative to the monitor.
     */
    @NotNull MonitorPosition monitorPosition() throws IllegalStateException;
    /**
     * Sets the position of this window relative to the current monitor.
     * @throws IllegalArgumentException When the given position is outside the range of an integer.
     * @throws ArithmeticException When the final position is outside the range of an integer.
     * @throws IllegalStateException When the window is not attached to a monitor.
     */
    void monitorPosition(@NotNull MonitorPosition position) throws IllegalArgumentException, ArithmeticException, IllegalStateException;
    /**
     * @throws IllegalStateException When the window is not attached to a monitor.
     * @return The current monitor this window is on.
     */
    @NotNull Monitor monitor() throws IllegalStateException;
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
    @Nullable WindowContentScale scale();
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
     * Sets the resizable option for this window.
     */
    void resizable(boolean resizable);
    /**
     * @return If this window is currently focused.
     */
    boolean focused();
    /**
     * Focuses this window.
     * @apiNote Use this method carefully, as it is very disruptive if input focus moves to another window "randomly". Use requestAttention() instead for a less disruptive way.
     */
    void focus();
    /**
     * Requests attention to this window from the user.
     */
    void requestAttention();

    /**
     * @return If the window should close.
     */
    boolean shouldClose();
    /**
     * Sets the flag so the window closes.
     */
    void close();

    /**
     * Swaps the buffers of this window.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_swapBuffers(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Destroys this window.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_destroy(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException;
}