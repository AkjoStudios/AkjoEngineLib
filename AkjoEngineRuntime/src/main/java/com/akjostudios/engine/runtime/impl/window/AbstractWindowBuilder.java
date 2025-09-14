package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.window.Window;
import com.akjostudios.engine.api.window.WindowRegistryHook;
import com.akjostudios.engine.api.window.WindowVisibility;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public abstract class AbstractWindowBuilder {
    protected final String title;
    protected final Monitor monitor;
    protected final boolean vsync;

    private final FrameScheduler renderScheduler;
    private final EventBus events;

    protected WindowRegistryHook hook;

    protected @NotNull Window createWindow(
            int finalX, int finalY,
            int finalWidth, int finalHeight,
            WindowVisibility finalVisibility
    ) {
        long handle = getHandle(finalWidth, finalHeight, false);
        GLFW.glfwSetWindowPos(handle, finalX, finalY);

        Window window = makeContextCurrent(handle);
        window.visibility(finalVisibility);
        hook.register(window);
        return window;
    }

    protected @NotNull Window createWindow(int finalWidth, int finalHeight) {
        long handle = getHandle(finalWidth, finalHeight, true);

        Window window = makeContextCurrent(handle);
        window.visibility(WindowVisibility.DEFAULT);
        hook.register(window);
        return window;
    }

    private long getHandle(int finalWidth, int finalHeight, boolean fullscreen) {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        long handle = GLFW.glfwCreateWindow(
                finalWidth, finalHeight,
                title,
                fullscreen ? monitor.handle() : 0L,
                0L
        );
        if (handle == 0L) { throw new IllegalStateException("❗ Failed to create GLFW window!"); }

        return handle;
    }

    private @NotNull Window makeContextCurrent(long handle) {
        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
        return new WindowImpl(handle, renderScheduler, events);
    }
}