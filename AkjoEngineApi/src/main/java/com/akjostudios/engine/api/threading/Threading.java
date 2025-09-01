package com.akjostudios.engine.api.threading;

import com.akjostudios.engine.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused", "RedundantThrows"})
public interface Threading {
    void runOnRender(@NotNull Runnable task);
    void runOnAudio(@NotNull Runnable task);
    void runOnWorker(@NotNull Runnable task);
    <T> @NotNull CompletableFuture<T> runOnWorker(@NotNull Callable<T> task);

    boolean isRenderThread();
    boolean isLogicThread();
    boolean isAudioThread();

    /**
     * Initializes the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread AND only once.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    void __engine_init(
            @NotNull Object token,
            @NotNull Thread.UncaughtExceptionHandler handler,
            @NotNull Logger logger
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Starts the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread AND only once.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the runtime thread.
     */
    void __engine_start(
            @NotNull Object token,
            @NotNull Config config,
            @NotNull LogicCallback logicCallback
    ) throws IllegalCallerException, IllegalStateException;

    /**
     * Stops the threading implementation.
     * @apiNote Must be called by the runtime implementation of the engine AND only once.
     * @throws IllegalCallerException When this method is called externally.
     */
    void __engine_stop(
            @NotNull Object token
    ) throws IllegalCallerException;

    record Config(
            int workerThreads,
            double logicHz
    ) {}

    @FunctionalInterface
    interface LogicCallback {
        void onUpdate(double deltaTime) throws Exception;
    }
}