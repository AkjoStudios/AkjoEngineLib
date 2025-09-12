package com.akjostudios.engine.api.monitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface MonitorRegistry {
    /**
     * @apiNote This method does not work in the initialization phase.
     * @return A list of all currently connected monitors.
     */
    @NotNull List<Monitor> getMonitors();
    /**
     * @apiNote This method does not work in the initialization phase and must be called from the render thread.
     * @throws IllegalStateException When this method is not called from the render thread.
     * @return The monitor marked as primary.
     */
    @NotNull Monitor getPrimaryMonitor() throws IllegalStateException;
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
    void __engine_init(@NotNull Object token) throws IllegalCallerException, IllegalStateException;

    /**
     * Stops the monitor registry and unloads all connected monitors.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    void __engine_stop(@NotNull Object token) throws IllegalCallerException, IllegalStateException;
}