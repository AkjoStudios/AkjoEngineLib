package com.akjostudios.engine.api.time;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Time {
    /**
     * @return The current value of the system's high-resolution time source, in nanoseconds.
     */
    long nowNanos();

    /**
     * @return The current value of the system's high-resolution time source, in seconds.
     */
    double nowSeconds();

    /**
     * @return The time instance for the render thread.
     */
    @NotNull ThreadTime render();

    /**
     * @return The time instance for the logic thread.
     */
    @NotNull ThreadTime logic();

    /**
     * @return The time instance for the audio thread.
     */
    @NotNull ThreadTime audio();

    /**
     * Sets the scale that will be applied to the time values reported by the time instances.
     */
    void setScale(double scale);

    /**
     * @return The scale that will be applied to the time values reported by the time instances.
     */
    double getScale();

    /**
     * Pauses or resumes the time instances.
     */
    void setPaused(boolean paused);

    /**
     * @return If the time instances are paused.
     */
    boolean isPaused();
}