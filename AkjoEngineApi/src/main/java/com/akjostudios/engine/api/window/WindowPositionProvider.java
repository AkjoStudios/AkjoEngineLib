package com.akjostudios.engine.api.window;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface WindowPositionProvider {
    @NotNull WindowPosition retrieve(@NotNull Window window);
}