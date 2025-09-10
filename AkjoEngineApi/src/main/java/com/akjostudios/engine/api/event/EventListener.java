package com.akjostudios.engine.api.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue", "RedundantThrows"})
public interface EventListener<T extends Event> {
    void onEvent(@NotNull T event) throws Exception;
}