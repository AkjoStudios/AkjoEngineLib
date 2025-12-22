package com.akjostudios.engine.runtime.impl;

import com.akjostudios.engine.api.IAkjoApplicationContext;
import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.lifecycle.Lifecycle;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.monitor.MonitorRegistry;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.resource.file.MountableFileSystem;
import com.akjostudios.engine.api.scheduling.Scheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.api.time.Time;
import com.akjostudios.engine.api.window.WindowRegistry;
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
    private MountableFileSystem fs;
    private AssetManager assets;
    private MonitorRegistry monitors;
    private WindowRegistry windows;

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

    @Override
    public @NotNull MountableFileSystem fs() {
        if (fs == null) {
            throw new IllegalStateException("❗ File system object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return fs;
    }

    @Override
    public @NotNull AssetManager assets() {
        if (assets == null) {
            throw new IllegalStateException("❗ Asset manager object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return assets;
    }

    @Override
    public @NotNull MonitorRegistry monitors() {
        if (monitors == null) {
            throw new IllegalStateException("❗ Monitor registry object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return monitors;
    }

    @Override
    public @NotNull WindowRegistry windows() {
        if (windows == null) {
            throw new IllegalStateException("❗ Window registry object was requested before it exists! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return windows;
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

    /**
     * Sets the internal router file system for this application
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setFileSystem(
            @NotNull Object token,
            @NotNull MountableFileSystem fs
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ File system object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.fs = fs;
    }

    /**
     * Sets the internal asset manager for this application
     * @apiNote  Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setAssetManager(
            @NotNull Object token,
            @NotNull AssetManager assets
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Asset manager object set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.assets = assets;
    }

    /**
     * Sets the internal monitor registry for this application
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setMonitors(
            @NotNull Object token,
            @NotNull MonitorRegistry monitors
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Monitor registry set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.monitors = monitors;
    }

    /**
     * Sets the internal window registry for this application
     * @apiNote Must be called by the runtime implementation of the engine AND from the main thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the main thread.
     */
    @Override
    public void __engine_setWindows(
            @NotNull Object token,
            @NotNull WindowRegistry windows
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), MAIN_THREAD_NAME)) {
            throw new IllegalStateException("❗ Window registry set outside of main thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        this.windows = windows;
    }
}