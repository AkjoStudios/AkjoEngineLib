package com.akjostudios.engine.api.logging;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface LoggerProvider {
    @NotNull Logger retrieve(@NotNull String name);
}