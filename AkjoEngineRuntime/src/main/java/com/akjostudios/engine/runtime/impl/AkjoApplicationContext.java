package com.akjostudios.engine.runtime.impl;

import com.akjostudios.engine.api.IAkjoApplicationContext;
import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.api.time.Time;
import com.akjostudios.engine.runtime.impl.logging.LoggerImpl;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.DEFAULT_MAIN_THREAD_NAME;
import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.MAIN_THREAD_NAME;

public final class AkjoApplicationContext implements IAkjoApplicationContext {
    private static final String DEFAULT_BASE_LOGGER_NAME = "app";

    @Setter
    private volatile String baseLoggerName = DEFAULT_BASE_LOGGER_NAME;

    private Lifecycle lifecycle;
    private Threading threading;
    private Scheduler scheduler;
    private Time time;
    private EventBus events;

    @Override
    public @NotNull Logger logger() {
        String base = this.baseLoggerName;
        if (base == null || base.isBlank()) base = DEFAULT_BASE_LOGGER_NAME;
        return new LoggerImpl(base);
    }

    @Override
    public @NotNull Logger logger(@NotNull String name) {
        String base = this.baseLoggerName;
        if (base == null || base.isBlank()) { base = "app"; }
        String fullName = name.startsWith(base) ? name : base + " / " + name;
        return new LoggerImpl(fullName);
    }

    @Override
    public @NotNull Lifecycle lifecycle() {
        if (lifecycle == null) {
            throw new IllegalStateException("❗ No lifecycle object has been set! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return lifecycle;
    }

    @Override
    public @NotNull Threading threading() {
        if (threading == null) {
            throw new IllegalStateException("❗ Threading object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return threading;
    }

    @Override
    public @NotNull Scheduler scheduler() {
        if (scheduler == null) {
            throw new IllegalStateException("❗ Scheduler object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return scheduler;
    }

    @Override
    public @NotNull Time time() {
        if (time == null) {
            throw new IllegalStateException("❗ Time object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return time;
    }

    @Override
    public @NotNull EventBus events() {
        if (events == null) {
            throw new IllegalStateException("❗ Event bus object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return events;
    }

    /**
     * Sets the internal lifecycle object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setLifecycle(
            @NotNull Object token,
            @NotNull Lifecycle lifecycle
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), DEFAULT_MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Lifecycle object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.lifecycle = lifecycle;
    }

    /**
     * Sets the internal threading object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setThreading(
            @NotNull Object token,
            @NotNull Threading threading
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), DEFAULT_MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Threading object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.threading = threading;
    }

    /**
     * Sets the internal scheduling object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setScheduler(
            @NotNull Object token,
            @NotNull Scheduler scheduler
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Scheduler object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.scheduler = scheduler;
    }

    /**
     * Sets the internal time object for this application.
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setTime(
            @NotNull Object token,
            @NotNull Time time
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), DEFAULT_MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Scheduler object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.time = time;
    }

    /**
     * Sets the internal event bus for this application
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setEventBus(
            @NotNull Object token,
            @NotNull EventBus events
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Event bus object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.events = events;
    }
}