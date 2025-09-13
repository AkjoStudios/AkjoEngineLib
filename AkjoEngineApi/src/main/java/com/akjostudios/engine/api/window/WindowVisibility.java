package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.HasVisibility;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowVisibility(Type type, boolean visible) implements HasVisibility {
    public static WindowVisibility DEFAULT = new WindowVisibility(Type.REGULAR, true);

    @Override
    public @NotNull String toString() {
        return "WindowVisibility(" + type.name() + ", " + visible + ")";
    }

    public enum Type { REGULAR, MINIMIZED, MAXIMIZED }
}