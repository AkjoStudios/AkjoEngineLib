package com.akjostudios.engine.api.lifecycle;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Lifecycle {
    default void stopApplication() { stopApplication(null, "No reason provided"); }
    default void stopApplication(@Nullable String reason) { stopApplication(null, reason); }
    default void stopApplication(@Nullable Throwable throwable) { stopApplication(throwable, "No reason provided"); }
    void stopApplication(@Nullable Throwable throwable, @Nullable String reason);
    boolean isRunning();
    boolean isStopping();
}