package com.akjostudios.engine.api.window;

import com.akjostudios.engine.api.common.base.scale.IScale2D;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record WindowContentScale(double scaleX, double scaleY) implements IScale2D {
    public WindowContentScale {
        if (scaleX < 0 || scaleY < 0) {
            throw new IllegalArgumentException("❗ Window content scale must be positive.");
        }
    }

    @Override
    public @NotNull String toString() {
        return "WindowContentScale(" + scaleX + ", " + scaleY + ")";
    }
}