package com.akjostudios.engine.api;

import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.api.time.Time;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface IAkjoApplicationContext {
    // Main APIs
    @NotNull Lifecycle lifecycle();
    @NotNull Threading threading();
    @NotNull Scheduler scheduler();
    @NotNull Time time();

    // Logging
    @NotNull Logger logger();
    @NotNull Logger logger(@NotNull String name);
    default @NotNull Logger logger(@NotNull Class<?> type) {
        return logger(type.getSimpleName());
    }

    /**
     * Sets the internal lifecycle object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    void __engine_setLifecycle(
            @NotNull Object token,
            @NotNull Lifecycle lifecycle
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Sets the internal threading object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    void __engine_setThreading(
            @NotNull Object token,
            @NotNull Threading threading
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Sets the internal scheduling object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    void __engine_setScheduler(
            @NotNull Object token,
            @NotNull Scheduler scheduler
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Sets the internal time object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    void __engine_setTime(
            @NotNull Object token,
            @NotNull Time time
    ) throws IllegalCallerException, IllegalStateException;
}