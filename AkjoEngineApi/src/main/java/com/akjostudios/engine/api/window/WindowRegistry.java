package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.monitor.MonitorProvider;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.window.builder.WindowBuilder;
import com.akjostudios.engine.api.window.builder.WindowedWindowBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowRegistry {
    String DEFAULT_TITLE = "Untitled Window";
    WindowMode<WindowedWindowBuilder> DEFAULT_MODE = WindowMode.WINDOWED;
    boolean DEFAULT_VSYNC = true;

    /**
     * @throws IllegalArgumentException When the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default @NotNull WindowedWindowBuilder builder(
            @NotNull MonitorProvider monitor
    ) throws IllegalArgumentException {
        return builder(DEFAULT_TITLE, DEFAULT_MODE, monitor, DEFAULT_VSYNC);
    }
    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN) or the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default <T extends WindowBuilder> @NotNull T builder(
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor
    ) throws IllegalArgumentException {
        return builder(DEFAULT_TITLE, mode, monitor, DEFAULT_VSYNC);
    }
    /**
     * @throws IllegalArgumentException When the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default @NotNull WindowedWindowBuilder builder(
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) throws IllegalArgumentException {
        return builder(DEFAULT_TITLE, DEFAULT_MODE, monitor, vsync);
    }
    /**
     * @throws IllegalArgumentException When the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default @NotNull WindowedWindowBuilder builder(
            @NotNull String title,
            @NotNull MonitorProvider monitor
    ) throws IllegalArgumentException {
        return builder(title, DEFAULT_MODE, monitor, DEFAULT_VSYNC);
    }
    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN) or the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default <T extends WindowBuilder> @NotNull T builder(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor
    ) throws IllegalArgumentException {
        return builder(title, mode, monitor, DEFAULT_VSYNC);
    }
    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN) or the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default <T extends WindowBuilder> @NotNull T builder(
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) throws IllegalArgumentException {
        return builder(DEFAULT_TITLE, mode, monitor, vsync);
    }
    /**
     * @throws IllegalArgumentException When the monitor is not available.
     * @return A window builder for the given parameters.
     */
    default @NotNull WindowedWindowBuilder builder(
            @NotNull String title,
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) throws IllegalArgumentException {
        return builder(title, DEFAULT_MODE, monitor, vsync);
    }
    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN) or the monitor is not available.
     * @return A window builder for the given parameters.
     */
    <T extends WindowBuilder> @NotNull T builder(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) throws IllegalArgumentException;
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return A list of all registered windows.
     */
    @NotNull List<Window> getWindows();
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return The window with the given id/handle.
     */
    @Nullable Window getWindowById(long id);

    /**
     * Initializes the window registry.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_init(
            @NotNull Object token,
            @NotNull FrameScheduler renderScheduler,
            @NotNull EventBus events
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Stops the window registry.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_stop(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException;
}