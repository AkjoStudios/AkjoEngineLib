package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.MonitorPositionProvider;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.window.*;
import com.akjostudios.engine.api.window.builder.WindowedWindowBuilder;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class WindowedWindowBuilderImpl extends AbstractWindowBuilder implements WindowedWindowBuilder {
    private WindowResolutionProvider resolution = WindowResolution::DEFAULT;
    private MonitorPositionProvider position = MonitorPosition::CENTER;
    private WindowVisibility visibility = WindowVisibility.DEFAULT;
    private WindowOptions options = WindowOptions.DEFAULT;

    public WindowedWindowBuilderImpl(
            @NotNull String title, @NotNull Monitor monitor, boolean vsync,
            @NotNull FrameScheduler renderScheduler, @NotNull EventBus events
            ) {
        super(title, monitor, vsync, renderScheduler, events);
    }

    @Override
    public @NotNull WindowedWindowBuilder resolution(@NotNull WindowResolution resolution) {
        this.resolution = monitor -> resolution;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder resolution(@NotNull WindowResolutionProvider provider) {
        this.resolution = provider;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder position(@NotNull ScreenPosition position) {
        this.position = (monitor, resolution) -> new MonitorPosition(
                monitor,
                Math.subtractExact(position.x(), monitor.position().x()),
                Math.subtractExact(position.y(), monitor.position().y())
        );
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder position(@NotNull MonitorPosition position) {
        this.position = (monitor, resolution) -> position;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder position(@NotNull MonitorPositionProvider provider) {
        this.position = provider;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder visibility(@NotNull WindowVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder options(@NotNull WindowOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Instantiates the windowed window with the given parameters and registers it in the window registry.
     * @apiNote This method does not work in the initialization phase and must be called from the render thread.
     * @throws ArithmeticException When this method cannot calculate a valid position for the window.
     * @throws IllegalStateException When this method is not called from the render thread.
     * @return The newly created window.
     */
    @Override
    public @NotNull Window build() throws ArithmeticException, IllegalStateException {
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ WindowedWindowBuilder.build() can only be called from the render thread!");
        }
        if (hook == null) {
            throw new IllegalStateException("❗ Cannot build window without a registry hook! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        final WindowResolution finalResolution = resolution.retrieve(monitor);
        final MonitorPosition finalPosition = position.retrieve(monitor, finalResolution);
        final WindowVisibility finalVisibility = visibility;
        final WindowOptions finalOptions = options;

        final ScreenPosition basePosition = monitor.position();
        final int finalX = Math.toIntExact(Math.addExact(basePosition.x(), finalPosition.x()));
        final int finalY = Math.toIntExact(Math.addExact(basePosition.y(), finalPosition.y()));
        final int finalWidth = finalResolution.width();
        final int finalHeight = finalResolution.height();

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, finalOptions.decorated() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, finalOptions.resizable() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, finalOptions.floating() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        return createWindow(finalX, finalY, finalWidth, finalHeight, finalVisibility);
    }

    /**
     * Sets the internal registry hook for the window.
     * @apiNote Must be called by the runtime implementation of the engine.
     * @throws IllegalCallerException When this method is called externally.
     */
    @Override
    public void __engine_setRegistryHook(
            @NotNull Object token,
            @NotNull WindowRegistryHook hook
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        this.hook = hook;
    }

    @Override
    public String toString() {
        final WindowResolution finalResolution = resolution.retrieve(monitor);
        final MonitorPosition finalPosition = position.retrieve(monitor, finalResolution);
        return "WindowedWindowBuilder(" +
                "title='" + title + "'" +
                ", monitor=" + monitor +
                ", vsync=" + vsync +
                ", resolution=" + finalResolution +
                ", position=" + finalPosition +
                ", visibility=" + visibility +
                ", options=" + options +
                ")";
    }
}