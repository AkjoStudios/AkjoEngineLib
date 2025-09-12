package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.scale.IScale2D;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record MonitorContentScale(double scaleX, double scaleY) implements IScale2D {
    public MonitorContentScale {
        if (scaleX <= 0 || scaleY <= 0) {
            throw new IllegalArgumentException("❗ Monitor content scale must be positive.");
        }
    }

    @Override
    public @NotNull String toString() {
        return "MonitorContentScale(" + scaleX + ", " + scaleY + ")";
    }
}