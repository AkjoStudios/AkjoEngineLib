package com.akjostudios.engine.runtime.impl.window;

import com.akjostudios.engine.api.canvas.Canvas;
import com.akjostudios.engine.api.common.cancel.Cancellable;
import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.event.EventLane;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.logging.Logger;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorPosition;
import com.akjostudios.engine.api.monitor.MonitorPositionProvider;
import com.akjostudios.engine.api.monitor.ScreenPosition;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import com.akjostudios.engine.api.threading.Threading;
import com.akjostudios.engine.api.window.*;
import com.akjostudios.engine.api.window.events.*;
import com.akjostudios.engine.runtime.commands.render.ClearCommand;
import com.akjostudios.engine.runtime.impl.canvas.CanvasImpl;
import com.akjostudios.engine.runtime.impl.logging.LoggerImpl;
import com.akjostudios.engine.runtime.impl.monitor.MonitorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class WindowImpl implements Window {
    private final Logger log;

    private final long handle;

    private final CanvasImpl canvas;

    private final FrameScheduler renderScheduler;
    private final Threading threading;
    private final EventBus events;

    private final AtomicBoolean renderRequested = new AtomicBoolean(true);
    private final List<Runnable> renderCallbacks = new CopyOnWriteArrayList<>();

    private final AtomicReference<WindowState> state = new AtomicReference<>();

    private final AtomicReference<GLFWWindowPosCallback> posCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowSizeCallback> sizeCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowCloseCallback> closeCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowRefreshCallback> refreshCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowFocusCallback> focusCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowIconifyCallback> iconifyCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowMaximizeCallback> maximizeCallback = new AtomicReference<>();
    private final AtomicReference<GLFWWindowContentScaleCallback> contentScaleCallback = new AtomicReference<>();

    private GLCapabilities capabilities;

    public WindowImpl(
            long handle,
            @NotNull FrameScheduler renderScheduler,
            @NotNull Threading threading,
            @NotNull EventBus events
    ) {
        this.log = new LoggerImpl("Window[" + handle + "]");

        this.handle = handle;

        this.canvas = new CanvasImpl(() -> {
            renderRequested.set(true);
            threading.requestRender();
        });

        this.renderScheduler = renderScheduler;
        this.threading = threading;
        this.events = events;

        this.renderScheduler.immediate(() -> {
            WindowState initState = new WindowState(
                    queryName(),
                    queryPosition(),
                    queryMonitor(),
                    queryResolution(),
                    queryScale(),
                    queryVisibility(),
                    queryOptions(),
                    queryFocused(),
                    false
            );
            state.set(initState);
            init();
        });
    }

    @Override
    public long handle() { return handle; }

    @Override
    public @NotNull Canvas canvas() { return canvas; }

    @Override
    public @NotNull String name() {
        if (state.get() == null) { return queryName(); }
        return state.get().name();
    }

    private @NotNull String queryName() {
        String name = null;
        try {
            name = GLFW.glfwGetWindowTitle(handle);
        } catch (Exception _) {}
        return name == null ? "Unknown" : name;
    }

    @Override
    public void name(@NotNull String name) {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            GLFW.glfwSetWindowTitle(handle, name);

            WindowState previous = state.getAndUpdate(current -> new WindowState(
                    name, current.position(), current.monitor(), current.resolution(),
                    current.scale(), current.visibility(), current.options(),
                    current.focused(), current.requestedAttention()
            ));

            events.publish(new WindowTitleChangedEvent(this, previous.name()));
        });
    }

    @Override
    public @NotNull ScreenPosition position() {
        if (state.get() == null) { return queryPosition(); }
        return state.get().position();
    }

    private @NotNull ScreenPosition queryPosition() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);

            GLFW.glfwGetWindowPos(handle, px, py);

            return new ScreenPosition(px.get(0), py.get(0));
        } catch (Exception _) {
            return new ScreenPosition(0, 0);
        }
    }

    @Override
    public void position(@NotNull ScreenPosition position) throws IllegalArgumentException {
        if (position.x() > Integer.MAX_VALUE
                || position.y() > Integer.MAX_VALUE
                || position.x() < Integer.MIN_VALUE
                || position.y() < Integer.MIN_VALUE
        ) { throw new IllegalArgumentException("❗ Window position must be within the range of an integer!"); }

        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            GLFW.glfwSetWindowPos(handle, (int) position.x(), (int) position.y());

            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), position, current.monitor(), current.resolution(),
                        current.scale(), current.visibility(), current.options(),
                        current.focused(), current.requestedAttention()
                );
            });
        });
    }

    @Override
    public @NotNull MonitorPosition monitorPosition() throws IllegalStateException {
        return calculateMonitorPosition(monitor(), position());
    }

    @Override
    public void monitorPosition(@NotNull MonitorPosition position) throws IllegalArgumentException, ArithmeticException {
        if (position.x() > Integer.MAX_VALUE
            || position.y() > Integer.MAX_VALUE
            || position.x() < Integer.MIN_VALUE
            || position.y() < Integer.MIN_VALUE
        ) { throw new IllegalArgumentException("❗ Window position must be within the range of an integer!"); }

        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            ScreenPosition monitorPosition = position.monitor().position();

            int newX = Math.toIntExact(monitorPosition.x() + position.x());
            int newY = Math.toIntExact(monitorPosition.y() + position.y());
            ScreenPosition windowPosition = new ScreenPosition(newX, newY);

            GLFW.glfwSetWindowPos(handle, newX, newY);

            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), windowPosition, current.monitor(), current.resolution(),
                        current.scale(), current.visibility(), current.options(),
                        current.focused(), current.requestedAttention()
                );
            });
        });
    }

    @Override
    public void monitorPosition(@NotNull MonitorPositionProvider provider) throws IllegalArgumentException, ArithmeticException {
        monitorPosition(provider.retrieve(monitor(), resolution()));
    }

    private @NotNull MonitorPosition calculateMonitorPosition(
            @NotNull Monitor monitor,
            @NotNull ScreenPosition windowPosition
    ) {
        return new MonitorPosition(monitor,
                Math.subtractExact(windowPosition.x(), monitor.position().x()),
                Math.subtractExact(windowPosition.y(), monitor.position().y())
        );
    }

    @Override
    public @NotNull Monitor monitor() throws IllegalStateException {
        if (state.get() == null) { return queryMonitor(); }
        return state.get().monitor();
    }

    private @NotNull Monitor queryMonitor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer windowX = stack.mallocInt(1);
            IntBuffer windowY = stack.mallocInt(1);
            IntBuffer windowWidth = stack.mallocInt(1);
            IntBuffer windowHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowPos(handle, windowX, windowY);
            GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);

            long bestMonitor = 0L;
            int bestArea = -1;

            PointerBuffer monitors = GLFW.glfwGetMonitors();
            if (monitors != null) {
                for (int monitorIndex = 0; monitorIndex < monitors.limit(); monitorIndex++) {
                    long monitor = monitors.get(monitorIndex);

                    IntBuffer monitorX = stack.mallocInt(1);
                    IntBuffer monitorY = stack.mallocInt(1);
                    IntBuffer monitorWidth = stack.mallocInt(1);
                    IntBuffer monitorHeight = stack.mallocInt(1);

                    GLFW.glfwGetMonitorWorkarea(monitor, monitorX, monitorY, monitorWidth, monitorHeight);

                    if (monitorWidth.get(0) == 0 || monitorHeight.get(0) == 0) {
                        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(monitor);
                        if (videoMode == null) { continue; }
                        GLFW.glfwGetMonitorPos(monitor, monitorX, monitorY);
                    }

                    int areaX1 = Math.max(windowX.get(0), monitorX.get(0));
                    int areaY1 = Math.max(windowY.get(0), monitorY.get(0));
                    int areaX2 = Math.min(windowX.get(0) + windowWidth.get(0), monitorX.get(0) + monitorWidth.get(0));
                    int areaY2 = Math.min(windowY.get(0) + windowHeight.get(0), monitorY.get(0) + monitorHeight.get(0));

                    int area = Math.max(0, (areaX2 - areaX1) * (areaY2 - areaY1));
                    if (area > bestArea) {
                        bestArea = area;
                        bestMonitor = monitor;
                    }
                }
            }

            if (bestMonitor == 0L) { bestMonitor = GLFW.glfwGetPrimaryMonitor(); }

            return new MonitorImpl(bestMonitor, renderScheduler);
        } catch (Exception _) {
            return new MonitorImpl(-1L, renderScheduler);
        }
    }

    private void updateMonitor() {
        Monitor newMonitor = queryMonitor();

        state.updateAndGet(current -> {
            if (current == null) { return null; }
            if (current.monitor().handle() == newMonitor.handle()) { return current; }

            events.publish(new WindowMonitorChangedEvent(this, current.monitor()));

            return new WindowState(
                    current.name(), current.position(), newMonitor, current.resolution(),
                    current.scale(), current.visibility(), current.options(),
                    current.focused(), current.requestedAttention()
            );
        });
    }

    @Override
    public @NotNull WindowResolution resolution() {
        if (state.get() == null) { return queryResolution(); }
        return state.get().resolution();
    }

    @Override
    public void resolution(@NotNull WindowResolutionProvider provider) {
        resolution(provider.retrieve(monitor()));
    }

    private @NotNull WindowResolution queryResolution() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pwidth = stack.mallocInt(1);
            IntBuffer pheight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(handle, pwidth, pheight);

            return new WindowResolution(pwidth.get(0), pheight.get(0));
        } catch (Exception _) {
            return new WindowResolution(0, 0);
        }
    }

    @Override
    public void resolution(@NotNull WindowResolution resolution) {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            GLFW.glfwSetWindowSize(handle, resolution.width(), resolution.height());

            state.updateAndGet(current -> {
                if (current == null) { return null; }
                return new WindowState(
                        current.name(), current.position(), current.monitor(), resolution,
                        current.scale(), current.visibility(), current.options(),
                        current.focused(), current.requestedAttention()
                );
            });
        });
    }

    @Override
    public @Nullable WindowContentScale scale() {
        if (state.get() == null) { return queryScale(); }
        return state.get().scale();
    }

    private @Nullable WindowContentScale queryScale() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer pscaleX = stack.mallocFloat(1);
            FloatBuffer pscaleY = stack.mallocFloat(1);

            GLFW.glfwGetWindowContentScale(handle, pscaleX, pscaleY);

            if (pscaleX.get(0) == 0 || pscaleY.get(0) == 0) {
                return null;
            }

            return new WindowContentScale(pscaleX.get(0), pscaleY.get(0));
        } catch (Exception _) {
            return new WindowContentScale(0, 0);
        }
    }

    @Override
    public @NotNull WindowVisibility visibility() {
        if (state.get() == null) { return queryVisibility(); }
        return state.get().visibility();
    }

    private @NotNull WindowVisibility queryVisibility() {
        try {
            boolean minimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
            boolean maximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
            boolean visible = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;

            WindowVisibility.Type type = WindowVisibility.Type.REGULAR;
            if (minimized) { type = WindowVisibility.Type.MINIMIZED; }
            if (maximized) { type = WindowVisibility.Type.MAXIMIZED; }

            return new WindowVisibility(type, visible);
        } catch (Exception _) {
            return new WindowVisibility(WindowVisibility.Type.REGULAR, false);
        }
    }

    @Override
    public void visibility(@NotNull WindowVisibility visibility) {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            boolean wantVisible = visibility.visible();
            WindowVisibility.Type type = visibility.type();

            boolean isVisible = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
            boolean isMinimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
            boolean isMaximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;

            if (!wantVisible) {
                if (isMinimized || isMaximized) { GLFW.glfwRestoreWindow(handle); }
                if (isVisible) {
                    GLFW.glfwHideWindow(handle);
                    events.publish(new WindowHiddenEvent(this));
                }
                return;
            }

            if (!isVisible) {
                GLFW.glfwShowWindow(handle);
                GLFW.glfwFocusWindow(handle);
                events.publish(new WindowShownEvent(this));
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

            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), visibility, current.options(),
                        current.focused(), current.requestedAttention()
                );
            });
        });
    }

    @Override
    public @NotNull WindowOptions options() {
        if (state.get() == null) { return queryOptions(); }
        return state.get().options();
    }

    private @NotNull WindowOptions queryOptions() {
        try {
            boolean isResizable = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_RESIZABLE) == GLFW.GLFW_TRUE;
            boolean isDecorated = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_DECORATED) == GLFW.GLFW_TRUE;
            boolean isFloating = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FLOATING) == GLFW.GLFW_TRUE;

            return new WindowOptions(isResizable, isDecorated, isFloating);
        } catch (Exception _) {
            return new WindowOptions(false, false, false);
        }
    }

    @Override
    public void resizable(boolean resizable) {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            WindowState previous = state.getAndUpdate(current -> new WindowState(
                    current.name(), current.position(), current.monitor(), current.resolution(),
                    current.scale(), current.visibility(), new WindowOptions(
                            resizable, current.options().decorated(), current.options().floating()
                    ), current.focused(), current.requestedAttention()
            ));

            events.publish(new WindowOptionsChangedEvent(this, previous.options()));
        });
    }

    @Override
    public boolean focused() {
        if (state.get() == null) { return queryFocused(); }
        return state.get().focused();
    }

    private boolean queryFocused() {
        try {
            return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    public void focus() {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }
            if (focused()) { return; }

            GLFW.glfwFocusWindow(handle);

            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), current.visibility(), current.options(),
                        true, false
                );
            });
        });
    }

    @Override
    public void requestAttention() {
        renderScheduler.immediate(() -> {
            if (!GLFW.glfwInit()) { return; }

            GLFW.glfwRequestWindowAttention(handle);

            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), current.visibility(), current.options(),
                        current.focused(), true
                );
            });
        });
    }

    @Override
    public void requestRender() {
        renderRequested.set(true);
    }

    @Override
    public @NotNull Cancellable onRender(@NotNull Runnable callback) {
        renderCallbacks.add(callback);
        renderRequested.set(true);

        return new Cancellable() {
            private final AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public boolean cancel() {
                if (!cancelled.compareAndSet(false, true)) {
                    return false;
                }
                renderCallbacks.remove(callback);
                return true;
            }

            @Override
            public boolean isCancelled() { return cancelled.get(); }
        };
    }

    @Override
    public boolean shouldClose() { return GLFW.glfwWindowShouldClose(handle); }

    @Override
    public void close() {
        if (GLFW.glfwInit()) {
            GLFW.glfwSetWindowShouldClose(handle, true);
        }

        events.publish(new WindowCloseRequestedEvent(this));
    }

    @Override
    public String toString() {
        return "WindowImpl[" + handle + "]" + "(" +
                "name=" + name() + ", " +
                "position=" + position() + ", " +
                "resolution=" + resolution() + ", " +
                "scale=" + scale() + ", " +
                "visibility=" + visibility() + ", " +
                "options=" + options() + ")";
    }

    private void init() {
        if (!GLFW.glfwInit()) { return; }

        this.posCallback.set(GLFWWindowPosCallback.create((_, x, y) -> {
            WindowState previous = state.getAndUpdate(current -> new WindowState(
                    current.name(), new ScreenPosition(x, y), current.monitor(), current.resolution(),
                    current.scale(), current.visibility(), current.options(),
                    current.focused(), current.requestedAttention()
            ));
            updateMonitor();
            events.publish(new WindowMovedEvent(this, previous.position(), calculateMonitorPosition(
                    previous.monitor(),
                    previous.position()
            )));
        }));
        GLFW.glfwSetWindowPosCallback(handle, this.posCallback.get());

        this.sizeCallback.set(GLFWWindowSizeCallback.create((_, width, height) -> {
            WindowState previous = state.getAndUpdate(current -> new WindowState(
                    current.name(), current.position(), current.monitor(), new WindowResolution(width, height),
                    current.scale(), current.visibility(), current.options(),
                    current.focused(), current.requestedAttention()
            ));
            updateMonitor();
            renderRequested.set(true);
            threading.requestRender();
            events.publish(new WindowResizedEvent(this, previous.resolution()));
        }));
        GLFW.glfwSetWindowSizeCallback(handle, this.sizeCallback.get());

        this.closeCallback.set(GLFWWindowCloseCallback.create(
                (_) -> events.publish(new WindowCloseRequestedEvent(this))
        ));
        GLFW.glfwSetWindowCloseCallback(handle, this.closeCallback.get());

        this.refreshCallback.set(GLFWWindowRefreshCallback.create((_) -> {
            renderRequested.set(true);
            threading.requestRender();
        }));
        GLFW.glfwSetWindowRefreshCallback(handle, this.refreshCallback.get());

        this.focusCallback.set(GLFWWindowFocusCallback.create((_, focused) -> {
            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), current.visibility(), current.options(),
                        focused, false
                );
            });
            if (focused) { events.publish(new WindowFocusGainedEvent(this)); }
            else { events.publish(new WindowFocusLostEvent(this)); }
        }));
        GLFW.glfwSetWindowFocusCallback(handle, this.focusCallback.get());

        this.iconifyCallback.set(GLFWWindowIconifyCallback.create((_, iconified) -> {
            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), new WindowVisibility(
                                iconified ? WindowVisibility.Type.MINIMIZED : WindowVisibility.Type.REGULAR,
                                current.visibility().visible()
                        ), current.options(), current.focused(), current.requestedAttention()
                );
            });
            renderRequested.set(true);
            threading.requestRender();
            if (iconified) { events.publish(new WindowMinimizedEvent(this)); }
            else { events.publish(new WindowRestoredEvent(this)); }
        }));
        GLFW.glfwSetWindowIconifyCallback(handle, this.iconifyCallback.get());

        this.maximizeCallback.set(GLFWWindowMaximizeCallback.create((_, maximized) -> {
            state.updateAndGet(current -> {
                if (current == null) { return null; }

                return new WindowState(
                        current.name(), current.position(), current.monitor(), current.resolution(),
                        current.scale(), new WindowVisibility(
                                maximized ? WindowVisibility.Type.MAXIMIZED : WindowVisibility.Type.REGULAR,
                                current.visibility().visible()
                        ), current.options(), current.focused(), current.requestedAttention()
                );
            });
            renderRequested.set(true);
            threading.requestRender();
            if (maximized) { events.publish(new WindowMaximizedEvent(this)); }
            else { events.publish(new WindowRestoredEvent(this)); }
        }));
        GLFW.glfwSetWindowMaximizeCallback(handle, this.maximizeCallback.get());

        this.contentScaleCallback.set(GLFWWindowContentScaleCallback.create((_, scaleX, scaleY) -> {
            WindowState previous = state.getAndUpdate(current -> new WindowState(
                    current.name(), current.position(), current.monitor(), current.resolution(),
                    new WindowContentScale(scaleX, scaleY), current.visibility(), current.options(),
                    current.focused(), current.requestedAttention()
            ));
            updateMonitor();
            renderRequested.set(true);
            threading.requestRender();
            events.publish(new WindowContentScaleChangedEvent(this, previous.scale()));
        }));
        GLFW.glfwSetWindowContentScaleCallback(handle, this.contentScaleCallback.get());
    }

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
        if (capabilities == null) {
            capabilities = GL.createCapabilities();
        } else {
            GL.setCapabilities(capabilities);
        }
        events.publish(new WindowBeforeSwapBuffersEvent(this), EventLane.RENDER);

        GLFW.glfwSwapBuffers(handle);
        events.publish(new WindowAfterSwapBuffersEvent(this), EventLane.RENDER);
    }

    /**
     * Consumes the render request for the canvas of this window.
     * @apiNote Must be called by the runtime implementation of the engine
     * @throws IllegalCallerException When this method is called externally.
     */
    @Override
    public boolean __engine_consumeRenderRequested(
            @NotNull Object token
    ) throws IllegalCallerException {
        EngineTokens.verify(token);
        return renderRequested.getAndSet(false);
    }

    /**
     * Renders the canvas for this window.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_renderCanvas(
            @NotNull Object token
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ The canvas of a window must be rendered on the render thread!");
        }

        renderCallbacks.forEach(callback -> {
            try {
                callback.run();
            } catch (Throwable t) {
                log.error("An error occured inside a render callback!", t);
            }
        });

        final ClearCommand[] lastClear = { null };

        canvas.drainTo(command -> {
            if (command instanceof ClearCommand clear) {
                lastClear[0] = clear;
            } else {
                command.execute();
            }
        });

        if (lastClear[0] != null) {
            lastClear[0].execute();
        }
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

        GLFW.glfwMakeContextCurrent(handle);
        if (capabilities != null) {
            GL.setCapabilities(capabilities);
        }
        GLFW.glfwDestroyWindow(handle);

        if (this.posCallback.get() != null) {
            this.posCallback.get().free();
            this.posCallback.set(null);
        }

        if (this.sizeCallback.get() != null) {
            this.sizeCallback.get().free();
            this.sizeCallback.set(null);
        }

        if (this.closeCallback.get() != null) {
            this.closeCallback.get().free();
            this.closeCallback.set(null);
        }

        if (this.refreshCallback.get() != null) {
            this.refreshCallback.get().free();
            this.refreshCallback.set(null);
        }

        if (this.focusCallback.get() != null) {
            this.focusCallback.get().free();
            this.focusCallback.set(null);
        }

        if (this.iconifyCallback.get() != null) {
            this.iconifyCallback.get().free();
            this.iconifyCallback.set(null);
        }

        if (this.maximizeCallback.get() != null) {
            this.maximizeCallback.get().free();
            this.maximizeCallback.set(null);
        }

        if (this.contentScaleCallback.get() != null) {
            this.contentScaleCallback.get().free();
            this.contentScaleCallback.set(null);
        }
    }
}