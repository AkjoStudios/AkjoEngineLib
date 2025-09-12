package com.akjostudios.engine.api.monitor;

import com.akjostudios.engine.api.common.base.size.ISize2D;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public record MonitorSize(double widthMm, double heightMm) implements ISize2D {
    public MonitorSize {
        if (widthMm <= 0 || heightMm <= 0) {
            throw new IllegalArgumentException("❗ Monitor size must be positive.");
        }
    }
}