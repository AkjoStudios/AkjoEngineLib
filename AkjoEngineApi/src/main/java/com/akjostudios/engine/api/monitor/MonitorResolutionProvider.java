package com.akjostudios.engine.api.monitor;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MonitorResolutionProvider {
    @NotNull MonitorResolution retrieve();
}