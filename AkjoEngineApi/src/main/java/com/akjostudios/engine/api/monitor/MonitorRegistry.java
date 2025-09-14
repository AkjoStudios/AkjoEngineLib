package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MonitorRegistry {
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return A list of all currently connected monitors.
     */
    @NotNull List<Monitor> getMonitors();
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return The monitor marked as primary.
     */
    @Nullable Monitor getPrimaryMonitor();
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return The monitor with the given id/handle.
     */
    @Nullable Monitor getMonitorById(long id);

    /**
     * Initializes the monitor registry and loads all initially connected monitors.
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
     * Stops the monitor registry and unloads all connected monitors.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_stop(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException;
}