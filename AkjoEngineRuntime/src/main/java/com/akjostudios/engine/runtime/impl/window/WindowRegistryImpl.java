package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.LoggerProvider;
import com.akjostudios.engine.api.monitor.MonitorProvider;
import com.akjostudios.engine.api.render.backend.RenderBackendProvider;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowMode;
import com.akjostudios.engine.api.window.WindowRegistry;
import com.akjostudios.engine.api.window.WindowVisibility;
import com.akjostudios.engine.api.window.builder.BorderlessWindowBuilder;
import com.akjostudios.engine.api.window.builder.FullscreenWindowBuilder;
import com.akjostudios.engine.api.window.builder.WindowBuilder;
import com.akjostudios.engine.api.window.builder.WindowedWindowBuilder;
import com.akjostudios.engine.api.window.events.AllWindowsClosedEvent;
import com.akjostudios.engine.api.window.events.WindowCreatedEvent;
import com.akjostudios.engine.api.window.events.WindowDestroyedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class WindowRegistryImpl implements WindowRegistry {
    private final List<Window> windows = new CopyOnWriteArrayList<>();

    private final AtomicReference<RenderBackendProvider> backendProvider = new AtomicReference<>();

    private final AtomicReference<Threading> threading = new AtomicReference<>();
    private final AtomicReference<FrameScheduler> renderScheduler = new AtomicReference<>();
    private final AtomicReference<EventBus> events = new AtomicReference<>();
    private final AtomicReference<AssetManager> assets = new AtomicReference<>();

    private final AtomicReference<LoggerProvider> loggerProvider = new AtomicReference<>();

    /**
     * @return A future that returns the window for the given parameters.
     */
    @Override
    public <T extends WindowBuilder> @NotNull CompletableFuture<Window> create(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) {
        return createOnRenderThread(() -> builder(title, mode, monitor, vsync).build());
    }

    /**
     * @return A future that returns the window based on the given builder.
     */
    @Override
    public <T extends WindowBuilder> @NotNull CompletableFuture<Window> create(@NotNull T builder) {
        return createOnRenderThread(builder::build);
    }

    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN).
     * @return A window builder for the given parameters.
     */
    @Override
    public <T extends WindowBuilder> @NotNull T builder(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull MonitorProvider monitor,
            boolean vsync
    ) throws IllegalArgumentException {
        Class<T> builderType = mode.provide();
        if (builderType == WindowedWindowBuilder.class) {
            T impl = builderType.cast(new WindowedWindowBuilderImpl(
                    title, monitor, vsync,
                    backendProvider.get().retrieve(),
                    threading.get(),
                    renderScheduler.get(),
                    events.get(),
                    assets.get(),
                    loggerProvider.get()
            ));
            impl.__engine_setRegistryHook(EngineTokens.token(), this::addWindow);
            return impl;
        }
        if (builderType == BorderlessWindowBuilder.class) {
            T impl = builderType.cast(new BorderlessWindowBuilderImpl(
                    title, monitor, vsync,
                    backendProvider.get().retrieve(),
                    threading.get(),
                    renderScheduler.get(),
                    events.get(),
                    assets.get(),
                    loggerProvider.get()
            ));
            impl.__engine_setRegistryHook(EngineTokens.token(), this::addWindow);
            return impl;
        }
        if (builderType == FullscreenWindowBuilder.class) {
            T impl = builderType.cast(new FullscreenWindowBuilderImpl(
                    title, monitor, vsync,
                    backendProvider.get().retrieve(),
                    threading.get(),
                    renderScheduler.get(),
                    events.get(),
                    assets.get(),
                    loggerProvider.get()
            ));
            impl.__engine_setRegistryHook(EngineTokens.token(), this::addWindow);
            return impl;
        }
        throw new IllegalArgumentException("❗ Invalid window mode: " + mode + " (only WINDOWED, BORDERLESS and FULLSCREEN are supported!");
    }

    /**
     * @apiNote This method does not work in the initialization phase.
     * @return A list of all registered windows.
     */
    @Override
    public @NotNull List<Window> getWindows() { return windows; }

    /**
     * @apiNote This method does not work in the initialization phase.
     * @return The window with the given id/handle.
     */
    @Override
    public @Nullable Window getWindowById(long id) {
        return windows.stream()
                .filter(window -> window.handle() == id)
                .findFirst().orElse(null);
    }

    private void addWindow(@NotNull Window window) {
        windows.add(window);
        if (events.get() != null) { events.get().publish(new WindowCreatedEvent(window)); }
    }

    private @NotNull CompletableFuture<Window> createOnRenderThread(
            @NotNull Callable<Window> factory
    ) {
        FrameScheduler scheduler = renderScheduler.get();
        Threading thread = threading.get();

        if (scheduler == null || thread == null) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                    "❗ WindowRegistry is not initialized yet (render scheduler/threading missing)."
            ));
        }

        CompletableFuture<Window> future = new CompletableFuture<>();
        thread.requestRender();

        final int maxRetries = 300;
        final int[] retries = {0};

        Runnable attempt = new Runnable() {
            @Override
            public void run() {
                if (future.isDone()) { return; }

                try {
                    Window window = factory.call();
                    future.complete(window);
                } catch (IllegalArgumentException e) {
                    if (retries[0]++ < maxRetries) {
                        scheduler.afterFrames(1, this);
                        thread.requestRender();
                        return;
                    }
                    future.completeExceptionally(new IllegalArgumentException(
                            "❗ Failed to create window after " + maxRetries + " retries (monitor likely never became available)!", e
                    ));
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        };

        scheduler.immediate(attempt);
        return future;
    }

    /**
     * Initializes the window registry.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_init(
            @NotNull Object token,
            @NotNull RenderBackendProvider backendProvider,
            @NotNull Threading threading,
            @NotNull FrameScheduler renderScheduler,
            @NotNull EventBus events,
            @NotNull AssetManager assets,
            @NotNull LoggerProvider loggerProvider
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ Window registry is not being initialized on render thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        renderScheduler.__engine_addPostFrameTask(
                token, () -> {
                    if (!GLFW.glfwInit()) { return; }

                    windows.forEach(window -> {
                        boolean doSwap = true;
                        if (window instanceof WindowImpl impl) {
                            doSwap = impl.__engine_consumeRenderRequested(token);
                        }
                        if (window.visibility().type() == WindowVisibility.Type.MINIMIZED) {
                            doSwap = false;
                        }
                        if (doSwap) {
                            window.__engine_swapBuffers(token);
                        }

                        if (window.shouldClose()) {
                            window.__engine_destroy(token);
                            windows.remove(window);
                            if (this.events.get() != null) {
                                this.events.get().publish(new WindowDestroyedEvent(window));
                            }
                        }

                        if (windows.isEmpty()) {
                            if (this.events.get() != null) {
                                this.events.get().publish(new AllWindowsClosedEvent());
                            }
                        }
                    });
                }
        );

        this.backendProvider.set(backendProvider);

        this.renderScheduler.set(renderScheduler);
        this.threading.set(threading);
        this.events.set(events);
        this.assets.set(assets);

        this.loggerProvider.set(loggerProvider);
    }

    /**
     * Stops the window registry.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_stop(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ Window registry is not being stopped on render thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        windows.forEach(window -> window.__engine_destroy(token));
        windows.clear();
    }
}