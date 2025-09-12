package com.akjostudios.engine.api.monitor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public interface MonitorRegistry {
    @NotNull List<Monitor> getMonitors();
    @NotNull Monitor getPrimaryMonitor();
    @NotNull Monitor getMonitorById(long id);
}