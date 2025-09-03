package com.akjostudios.engine.api.time;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ThreadTime {
    /**
     * @return The index of this thread that represents ticks or frames.
     */
    long index();

    /**
     * @return The time in seconds since the last time this thread was updated.
     */
    double deltaTime();

    /**
     * @return The time in seconds scaled by the time scale of the time instance.
     */
    double scaledDeltaTime();

    /**
     * @return The lazily calculated average delta time of this thread.
     */
    double averageDeltaTime();

    /**
     * @return The time in nanoseconds since the last time this thread was updated.
     */
    long lastNowNanos();

    /**
     * @return The time in seconds since the last time this thread was updated.
     */
    double lastNowSeconds();
}