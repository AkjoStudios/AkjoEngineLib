package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.LoggerProvider;
import com.akjostudios.engine.api.monitor.*;
import com.akjostudios.engine.api.render.backend.RenderBackend;
import com.akjostudios.engine.api.resource.asset.AssetManager;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.threading.Threading;
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
            @NotNull String title,
            @NotNull MonitorProvider monitor,
            boolean vsync,
            @NotNull RenderBackend backend,
            @NotNull Threading threading,
            @NotNull FrameScheduler renderScheduler,
            @NotNull EventBus events,
            @NotNull AssetManager assets,
            @NotNull LoggerProvider loggerProvider
    ) {
        super(title, monitor, vsync, backend,
                threading, renderScheduler, events, assets, loggerProvider
        );
    }

    @Override
    public @NotNull WindowedWindowBuilder resolution(@NotNull WindowResolution resolution) {
        this.resolution = _ -> resolution;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder resolution(@NotNull WindowResolutionProvider provider) {
        this.resolution = provider;
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder position(@NotNull ScreenPosition position) {
        this.position = (monitor, _) -> new MonitorPosition(
                monitor,
                Math.subtractExact(position.x(), monitor.position().x()),
                Math.subtractExact(position.y(), monitor.position().y())
        );
        return this;
    }

    @Override
    public @NotNull WindowedWindowBuilder position(@NotNull MonitorPosition position) {
        this.position = (_, _) -> position;
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
            throw new IllegalStateException(
                    "❗ WindowedWindowBuilder.build() can only be called from the render thread!"
            );
        }
        if (hook == null) {
            throw new IllegalStateException("❗ Cannot build window without a registry hook! This is likely a bug in the engine.");
        }
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("❗ Cannot build window without GLFW being initialized!");
        }

        Monitor resolvedMonitor = monitor.get();
        if (resolvedMonitor == null) {
            throw new IllegalStateException("❗ Monitor is not available yet!");
        }

        final WindowResolution finalResolution = resolution.retrieve(resolvedMonitor);
        final MonitorPosition finalPosition = position.retrieve(resolvedMonitor, finalResolution);

        final ScreenPosition basePosition = resolvedMonitor.position();
        final int finalX = Math.toIntExact(Math.addExact(basePosition.x(), finalPosition.x()));
        final int finalY = Math.toIntExact(Math.addExact(basePosition.y(), finalPosition.y()));

        final WindowOptions finalOptions = options;
        final WindowVisibility finalVisibility = visibility;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(
                GLFW.GLFW_DECORATED,
                finalOptions.decorated() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE
        );
        GLFW.glfwWindowHint(
                GLFW.GLFW_RESIZABLE,
                finalOptions.resizable() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE
        );
        GLFW.glfwWindowHint(
                GLFW.GLFW_FLOATING,
                finalOptions.floating() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE
        );

        return createWindow(
                resolvedMonitor,
                finalX, finalY,
                finalResolution.width(), finalResolution.height(),
                finalVisibility
        );
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
        return "WindowedWindowBuilder(" +
                "title='" + title + "'" +
                ", monitor=" + monitor.get() +
                ", vsync=" + vsync +
                ", visibility=" + visibility +
                ", options=" + options +
                ")";
    }
}