package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.HasVisibility;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowVisibility(Type type, boolean visible) implements HasVisibility {
    public static final WindowVisibility DEFAULT = new WindowVisibility(Type.REGULAR, true);
    public static final WindowVisibility HIDDEN = new WindowVisibility(Type.REGULAR, false);
    public static final WindowVisibility MINIMIZED = new WindowVisibility(Type.MINIMIZED, true);
    public static final WindowVisibility MAXIMIZED = new WindowVisibility(Type.MAXIMIZED, true);

    @Override
    public @NotNull String toString() {
        return "WindowVisibility(type=" + type.name() + ", visible=" + visible + ")";
    }

    public enum Type { REGULAR, MINIMIZED, MAXIMIZED }
}