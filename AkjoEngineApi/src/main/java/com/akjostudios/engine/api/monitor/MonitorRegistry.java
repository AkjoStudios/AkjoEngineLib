package com.akjostudios.engine.api.monitor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public interface MonitorRegistry {
    /**
     * @apiNote This method does not work in the initialization phase.
     * @throws RuntimeException If called during the initialization phase.
     * @return A list of all currently connected monitors.
     */
    @NotNull List<Monitor> getMonitors() throws RuntimeException;
    /**
     * @apiNote This method does not work in the initialization phase.
     * @throws RuntimeException If called during the initialization phase.
     * @return The monitor marked as primary.
     */
    @NotNull Monitor getPrimaryMonitor() throws RuntimeException;
    /**
     * @apiNote This method does not work in the initialization phase.
     * @throws RuntimeException If called during the initialization phase.
     * @return The monitor with the given id/handle.
     */
    @NotNull Monitor getMonitorById(long id) throws RuntimeException;
}