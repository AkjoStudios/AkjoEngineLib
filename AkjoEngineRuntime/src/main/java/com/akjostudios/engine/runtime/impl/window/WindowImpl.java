package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.window.*;
import com.akjostudios.engine.runtime.impl.monitor.MonitorImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

@RequiredArgsConstructor
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public final class WindowImpl implements Window {
    private final long handle;

    @Override
    public long handle() { return handle; }

    @Override
    public @NotNull String name() {
        String name = GLFW.glfwGetWindowTitle(handle);
        return name == null ? "Unknown" : name;
    }

    @Override
    public void name(@NotNull String name) {
        GLFW.glfwSetWindowTitle(handle, name);
    }

    @Override
    public @NotNull ScreenPosition position() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(handle, px, py);
            return new ScreenPosition(px.get(0), py.get(0));
        }
    }

    @Override
    public void position(@NotNull ScreenPosition position) throws IllegalArgumentException {
        if (position.x() > Integer.MAX_VALUE
                || position.y() > Integer.MAX_VALUE
                || position.x() < Integer.MIN_VALUE
                || position.y() < Integer.MIN_VALUE
        ) { throw new IllegalArgumentException("❗ Window position must be within the range of an integer!"); }

        GLFW.glfwSetWindowPos(handle, (int) position.x(), (int) position.y());
    }

    @Override
    public @NotNull MonitorPosition monitorPosition() throws IllegalStateException {
        Monitor monitor = monitor();
        ScreenPosition windowPosition = position();
        ScreenPosition monitorPosition = monitor.position();

        return new MonitorPosition(
                monitor,
                Math.subtractExact(windowPosition.x(), monitorPosition.x()),
                Math.subtractExact(windowPosition.y(), monitorPosition.y())
        );
    }

    @Override
    public void monitorPosition(@NotNull MonitorPosition position) throws IllegalArgumentException, ArithmeticException, IllegalStateException {
        if (position.x() > Integer.MAX_VALUE
            || position.y() > Integer.MAX_VALUE
            || position.x() < Integer.MIN_VALUE
            || position.y() < Integer.MIN_VALUE
        ) { throw new IllegalArgumentException("❗ Window position must be within the range of an integer!"); }

        ScreenPosition monitorPosition = position.monitor().position();

        int newX = Math.toIntExact(monitorPosition.x() + position.x());
        int newY = Math.toIntExact(monitorPosition.y() + position.y());

        GLFW.glfwSetWindowPos(handle, newX, newY);
    }

    @Override
    public @NotNull Monitor monitor() throws IllegalStateException {
        long monitorHandle = GLFW.glfwGetWindowMonitor(handle);
        if (monitorHandle == 0) {
            throw new IllegalStateException("❗ Window is not attached to a monitor! This is likely a bug in the engine - please report it using the issue tracker.");
        }
        return new MonitorImpl(monitorHandle);
    }

    @Override
    public @NotNull WindowResolution resolution() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pwidth = stack.mallocInt(1);
            IntBuffer pheight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(handle, pwidth, pheight);
            return new WindowResolution(pwidth.get(0), pheight.get(0));
        }
    }

    @Override
    public void resolution(@NotNull WindowResolution resolution) {
        GLFW.glfwSetWindowSize(handle, resolution.width(), resolution.height());
    }

    @Override
    public @Nullable WindowContentScale scale() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer pscaleX = stack.mallocFloat(1);
            FloatBuffer pscaleY = stack.mallocFloat(1);
            GLFW.glfwGetWindowContentScale(handle, pscaleX, pscaleY);
            if (pscaleX.get(0) == 0 || pscaleY.get(0) == 0) {
                return null;
            }
            return new WindowContentScale(pscaleX.get(0), pscaleY.get(0));
        }
    }

    @Override
    public @NotNull WindowVisibility visibility() {
        boolean minimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        boolean maximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
        boolean visible = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;

        WindowVisibility.Type type = WindowVisibility.Type.REGULAR;
        if (minimized) { type = WindowVisibility.Type.MINIMIZED; }
        if (maximized) { type = WindowVisibility.Type.MAXIMIZED; }

        return new WindowVisibility(type, visible);
    }

    @Override
    public void visibility(@NotNull WindowVisibility visibility) {
        boolean wantVisible = visibility.visible();
        WindowVisibility.Type type = visibility.type();

        boolean isVisible = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
        boolean isMinimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        boolean isMaximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;

        if (!wantVisible) {
            if (isMinimized || isMaximized) { GLFW.glfwRestoreWindow(handle); }
            if (isVisible) { GLFW.glfwHideWindow(handle); }
            return;
        }

        if (!isVisible) {
            GLFW.glfwShowWindow(handle);
            GLFW.glfwFocusWindow(handle);
        }

        switch (type) {
            case REGULAR -> {
                if (isMinimized || isMaximized) { GLFW.glfwRestoreWindow(handle); }
            }
            case MINIMIZED -> {
                if (!isMinimized) { GLFW.glfwIconifyWindow(handle); }
            }
            case MAXIMIZED -> {
                if (isMinimized) { GLFW.glfwRestoreWindow(handle); }
                if (!isMaximized) { GLFW.glfwMaximizeWindow(handle); }
            }
        }
    }

    @Override
    public @NotNull WindowOptions options() {
        boolean isResizable = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_RESIZABLE) == GLFW.GLFW_TRUE;
        boolean isDecorated = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_DECORATED) == GLFW.GLFW_TRUE;
        boolean isFloating = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FLOATING) == GLFW.GLFW_TRUE;

        return new WindowOptions(isResizable, isDecorated, isFloating);
    }

    @Override
    public void resizable(boolean resizable) {
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
    }

    @Override
    public boolean shouldClose() { return GLFW.glfwWindowShouldClose(handle); }

    @Override
    public void close() { GLFW.glfwSetWindowShouldClose(handle, true); }

    /**
     * Swaps the buffers of this window.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_swapBuffers(
            @NotNull Object token
    ) {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ The buffers of a window must be swapped on the render thread!");
        }
        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapBuffers(handle);
    }

    /**
     * Destroys this window.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_destroy(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ A window must be destroyed on the render thread!");
        }
        GLFW.glfwDestroyWindow(handle);
    }
}