package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.common.base.position.HasPosition2D;
import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorWorkArea;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowRegistryHook;
import com.akjostudios.engine.api.window.WindowVisibility;
import com.akjostudios.engine.api.window.builder.BorderlessWindowBuilder;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class BorderlessWindowBuilderImpl extends AbstractWindowBuilder implements BorderlessWindowBuilder {
    private WindowVisibility visibility = WindowVisibility.DEFAULT;
    private boolean resizable = true;

    public BorderlessWindowBuilderImpl(
            @NotNull String title, @NotNull Monitor monitor, boolean vsync,
            @NotNull FrameScheduler renderScheduler, @NotNull EventBus events
    ) {
        super(title, monitor, vsync, renderScheduler, events);
    }

    @Override
    public @NotNull BorderlessWindowBuilder visibility(@NotNull WindowVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public @NotNull BorderlessWindowBuilder resizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    /**
     * Instantiates the borderless window with the given parameters and registers it in the window registry.
     * @apiNote This method does not work in the initialization phase and must be called from the render thread.
     * @throws ArithmeticException When this method cannot calculate a valid position for the window.
     * @throws IllegalStateException When this method is not called from the render thread.
     * @return The newly created window.
     */
    @Override
    public @NotNull Window build() throws ArithmeticException, IllegalStateException {
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ BorderlessWindowBuilderImpl.build() can only be called from the render thread!");
        }
        if (this.hook == null) {
            throw new IllegalStateException("❗ Cannot build window without a registry hook! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("❗ Cannot build window without GLFW being initialized!");
        }

        final WindowVisibility finalVisibility = visibility;
        final boolean finalResizable = resizable;

        MonitorWorkArea workArea = monitor.screenArea();
        HasPosition2D positionProvider = workArea != null ? workArea : monitor;
        final int finalX = Math.toIntExact(positionProvider.position().x());
        final int finalY = Math.toIntExact(positionProvider.position().y());
        final int finalWidth = monitor.resolution().width();
        final int finalHeight = monitor.resolution().height();

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, finalResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_FALSE);

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
        return "BorderlessWindowBuilder(" +
                "title='" + title + "'" +
                ", monitor=" + monitor +
                ", vsync=" + vsync +
                ", visibility=" + visibility +
                ", resizable=" + resizable +
                ")";
    }
}