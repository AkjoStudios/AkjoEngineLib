package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.render.IRenderPosition;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowPosition(@NotNull Window window, long x, long y) implements IRenderPosition {
    public static @NotNull WindowPositionProvider create(long x, long y) {
        return window -> new WindowPosition(window, x, y);
    }

    @Override
    public @NotNull String toString() {
        return "WindowPosition(" + x + ", " + y + ")";
    }
}