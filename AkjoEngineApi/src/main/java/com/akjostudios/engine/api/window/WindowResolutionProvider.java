package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowResolutionProvider {
    @NotNull WindowResolution retrieve(@NotNull Monitor monitor);
}