package com.akjostudios.engine.api.threading;

import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.scheduling.TickScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused", "RedundantThrows"})
public interface Threading {
    /**
     * Runs the given task on a worker thread without a return object.
     */
    void runOnWorker(@NotNull Runnable task);

    /**
     * Runs the given task on a worker thread and returns an object that reports its status and will complete in the future with a return value.
     */
    <T> @NotNull CompletableFuture<T> runOnWorker(@NotNull Callable<T> task);

    /**
     * @return If the current thread is the render thread.
     */
    boolean isRenderThread();

    /**
     * @return If the current thread is the logic thread.
     */
    boolean isLogicThread();

    /**
     * @return If the current thread is the audio thread.
     */
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
     * @throws IllegalStateException When this method is not called from the main thread.
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

    /**
     * Sets the render scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a frame scheduler of type FrameSchedulerImpl.
     */
    void __engine_setRenderScheduler(
            @NotNull Object token,
            @NotNull FrameScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException, IllegalArgumentException;

    /**
     * Sets the logic scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a tick scheduler of type TickSchedulerImpl.
     */
    void __engine_setLogicScheduler(
            @NotNull Object token,
            @NotNull TickScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException, IllegalArgumentException;

    /**
     * Sets the audio scheduler for the threading system.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     * @throws IllegalArgumentException When this method does not get a frame scheduler of type FrameSchedulerImpl.
     */
    void __engine_setAudioScheduler(
            @NotNull Object token,
            @NotNull FrameScheduler scheduler
    ) throws IllegalCallerException, IllegalStateException, IllegalArgumentException;

    record Config(
            int workerThreads,
            double logicHz
    ) {}

    @FunctionalInterface
    interface LogicCallback {
        void onUpdate(double deltaTime) throws Exception;
    }
}