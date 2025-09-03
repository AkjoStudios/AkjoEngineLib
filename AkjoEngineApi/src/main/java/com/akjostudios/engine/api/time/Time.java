package com.akjostudios.engine.api.time;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Time {
    long nowNanos();
    double nowSeconds();

    @NotNull ThreadTime render();
    @NotNull ThreadTime logic();
    @NotNull ThreadTime audio();

    void setScale(double scale);
    double getScale();
    void setPaused(boolean paused);
    boolean isPaused();
}