package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowMode;
import com.akjostudios.engine.api.window.WindowRegistry;
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
import org.lwjgl.opengl.GL;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class WindowRegistryImpl implements WindowRegistry {
    private final List<Window> windows = new CopyOnWriteArrayList<>();

    private final AtomicReference<EventBus> events = new AtomicReference<>();

    /**
     * @throws IllegalArgumentException When the given mode is not one of the standard ones (WINDOWED, BORDERLESS, FULLSCREEN).
     * @return A window builder for the given parameters.
     */
    @Override
    public <T extends WindowBuilder> @NotNull T builder(
            @NotNull String title,
            @NotNull WindowMode<T> mode,
            @NotNull Monitor monitor,
            boolean vsync
    ) throws IllegalArgumentException {
        Class<T> builderType = mode.provide();
        if (builderType == WindowedWindowBuilder.class) {
            T impl = builderType.cast(new WindowedWindowBuilderImpl(title, monitor, vsync));
            impl.__engine_setRegistryHook(EngineTokens.token(), window -> {
                this.addWindow(window);
                if (events.get() != null) { events.get().publish(new WindowCreatedEvent(window)); }
            });
            return impl;
        }
        if (builderType == BorderlessWindowBuilder.class) {
            T impl = builderType.cast(new BorderlessWindowBuilderImpl(title, monitor, vsync));
            impl.__engine_setRegistryHook(EngineTokens.token(), this::addWindow);
            return impl;
        }
        if (builderType == FullscreenWindowBuilder.class) {
            T impl = builderType.cast(new FullscreenWindowBuilderImpl(title, monitor, vsync));
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

    /**
     * Initializes the window registry.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_init(
            @NotNull Object token,
            @NotNull FrameScheduler renderScheduler,
            @NotNull EventBus events
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ Window registry is not being initialized on render thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        renderScheduler.__engine_addPostFrameTask(
                token, () -> {
                    if (GLFW.glfwInit()) {
                        windows.forEach(window -> {
                            window.__engine_swapBuffers(token);
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
                }
        );

        windows.forEach(window -> {
            GLFW.glfwMakeContextCurrent(window.handle());
            GL.createCapabilities();
        });

        this.events.set(events);
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