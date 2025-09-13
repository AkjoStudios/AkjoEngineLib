package com.akjostudios.engine.api.window;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowOptions(boolean resizable, boolean decorated, boolean floating) {
    public static final WindowOptions DEFAULT = new WindowOptions(true, true, false);
    public static final WindowOptions FIXED = new WindowOptions(false, true, false);
    public static final WindowOptions UNDECORATED = new WindowOptions(true, false, false);
    public static final WindowOptions POPUP = new WindowOptions(false, true, true);
    public static final WindowOptions EMPTY = new WindowOptions(false, false, false);
    public static final WindowOptions FLOATING = new WindowOptions(true, true, true);
    public static final WindowOptions EMPTY_FLOATING = new WindowOptions(false, false, true);

    @Override
    public @NotNull String toString() {
        return "WindowOptions(resizable=" + resizable + ", decorated=" + decorated + ", floating=" + floating + ")";
    }
}