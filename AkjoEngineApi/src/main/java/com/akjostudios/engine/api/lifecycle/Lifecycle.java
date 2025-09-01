package com.akjostudios.engine.api.lifecycle;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Lifecycle {
    /**
     * Stops the application without providing a reason.
     * @apiNote This method does not work in the initialization phase.
     */
    default void stopApplication() { stopApplication(null, "No reason provided"); }
    /**
     * Stops the application with a given reason.
     * @apiNote This method does not work in the initialization phase.
     */
    default void stopApplication(@Nullable String reason) { stopApplication(null, reason); }
    /**
     * Stops the application erroneously without providing a reason other than the error happening.
     * @apiNote This method does not work in the initialization phase.
     */
    default void stopApplication(@Nullable Throwable throwable) { stopApplication(throwable, "No reason provided"); }
    /**
     * Stops the application erroneously with a given reason.
     * @apiNote This method does not work in the initialization phase.
     */
    void stopApplication(@Nullable Throwable throwable, @Nullable String reason);

    /**
     * @return If the application is currently running.
     */
    boolean isRunning();

    /**
     * @return If the application is currently stopping.
     */
    boolean isStopping();
}