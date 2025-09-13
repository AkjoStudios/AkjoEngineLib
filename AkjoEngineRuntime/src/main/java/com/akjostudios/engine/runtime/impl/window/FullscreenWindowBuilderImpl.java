package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorResolution;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowRegistryHook;
import com.akjostudios.engine.api.window.builder.FullscreenWindowBuilder;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class FullscreenWindowBuilderImpl extends AbstractWindowBuilder implements FullscreenWindowBuilder {
    public FullscreenWindowBuilderImpl(@NotNull String title, @NotNull Monitor monitor, boolean vsync) {
        super(title, monitor, vsync);
    }

    /**
     * Instantiates the fullscreen window with the given parameters and registers it in the window registry.
     * @apiNote This method does not work in the initialization phase and must be called from the render thread.
     * @throws ArithmeticException When this method cannot calculate a valid position for the window.
     * @throws IllegalStateException When this method is not called from the render thread.
     * @return The newly created window.
     */
    @Override
    public @NotNull Window build() throws ArithmeticException, IllegalStateException {
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ FullscreenWindowBuilderImpl.build() can only be called from the render thread!");
        }
        if (this.hook == null) {
            throw new IllegalStateException("❗ Cannot build window without a registry hook! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        MonitorResolution finalResolution = monitor.resolution();
        GLFW.glfwDefaultWindowHints();

        return createWindow(finalResolution.width(), finalResolution.height());
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
        return "FullscreenWindowBuilder(" +
                "title='" + title + "'" +
                ", monitor=" + monitor +
                ", vsync=" + vsync +
                ")";
    }
}