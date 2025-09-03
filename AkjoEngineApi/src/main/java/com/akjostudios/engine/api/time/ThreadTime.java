package com.akjostudios.engine.api.time;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface ThreadTime {
    long index();

    double deltaTime();
    double scaledDeltaTime();
    double averageDeltaTime();

    long lastNowNanos();
    double lastNowSeconds();
}