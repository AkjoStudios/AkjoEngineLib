package com.akjostudios.engine.api.window;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowRegistryHook {
    void register(@NotNull Window window);
}