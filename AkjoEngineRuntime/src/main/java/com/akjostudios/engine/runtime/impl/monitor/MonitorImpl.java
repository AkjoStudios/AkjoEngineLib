package com.akjostudios.engine.runtime.impl.monitor;

import com.akjostudios.engine.api.monitor.*;
import com.akjostudios.engine.api.scheduling.FrameScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicReference;

public final class MonitorImpl implements Monitor {
    private final long handle;
    private final FrameScheduler renderScheduler;

    private final AtomicReference<MonitorState> state = new AtomicReference<>();

    public MonitorImpl(long handle, @NotNull FrameScheduler renderScheduler) {
        this.handle = handle;
        this.renderScheduler = renderScheduler;
        this.renderScheduler.immediate(() -> {
            MonitorState initState = new MonitorState(
                    queryName(),
                    queryPosition(),
                    queryResolution(),
                    queryRefreshRate(),
                    querySize(),
                    queryScale(),
                    queryWorkArea(),
                    1.0
            );
            state.set(initState);
        });
    }

    @Override
    public long handle() { return handle; }

    @Override
    public @NotNull String name() {
        if (state.get() == null) { return queryName(); }
        return state.get().name();
    }

    private @NotNull String queryName() {
        String name = GLFW.glfwGetMonitorName(handle);
        return name == null ? "Unknown" : name;
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
            GLFW.glfwGetMonitorPos(handle, px, py);
            return new ScreenPosition(px.get(0), py.get(0));
        }
    }

    @Override
    public @NotNull MonitorResolution resolution() {
        if (state.get() == null) { return queryResolution(); }
        return state.get().resolution();
    }

    private @NotNull MonitorResolution queryResolution() {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(handle);
        if (vidmode == null) {
            return new MonitorResolution(0, 0);
        }
        return new MonitorResolution(vidmode.width(), vidmode.height());
    }

    @Override
    public int refreshRate() {
        if (state.get() == null) { return queryRefreshRate(); }
        return state.get().refreshRate();
    }

    private int queryRefreshRate() {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(handle);
        if (vidmode == null) {
            return 0;
        }
        return vidmode.refreshRate();
    }

    @Override
    public @Nullable MonitorSize size() {
        if (state.get() == null) { return querySize(); }
        return state.get().size();
    }

    private @Nullable MonitorSize querySize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pwidth = stack.mallocInt(1);
            IntBuffer pheight = stack.mallocInt(1);
            GLFW.glfwGetMonitorPhysicalSize(handle, pwidth, pheight);
            if (pwidth.get(0) == 0 || pheight.get(0) == 0) {
                return null;
            }
            return new MonitorSize(pwidth.get(0), pheight.get(0));
        }
    }

    @Override
    public @Nullable MonitorContentScale scale() {
        if (state.get() == null) { return queryScale(); }
        return state.get().scale();
    }

    private @Nullable MonitorContentScale queryScale() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer pscaleX = stack.mallocFloat(1);
            FloatBuffer pscaleY = stack.mallocFloat(1);
            GLFW.glfwGetMonitorContentScale(handle, pscaleX, pscaleY);
            if (pscaleX.get(0) == 0 || pscaleY.get(0) == 0) {
                return null;
            }
            return new MonitorContentScale(pscaleX.get(0), pscaleY.get(0));
        }
    }

    @Override
    public @Nullable MonitorWorkArea screenArea() {
        if (state.get() == null) { return queryWorkArea(); }
        return state.get().workArea();
    }

    private @Nullable MonitorWorkArea queryWorkArea() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);
            IntBuffer pwidth = stack.mallocInt(1);
            IntBuffer pheight = stack.mallocInt(1);
            GLFW.glfwGetMonitorWorkarea(handle, px, py, pwidth, pheight);
            if (pwidth.get(0) == 0 || pheight.get(0) == 0) {
                return null;
            }
            return new MonitorWorkArea(new ScreenPosition(px.get(0), py.get(0)), new MonitorResolution(pwidth.get(0), pheight.get(0)));
        }
    }

    @Override
    public double gamma() {
        if (state.get() == null) { return 1.0; }
        return state.get().gamma();
    }

    @Override
    public void gamma(double gamma) {
        if (gamma <= 0.0 || Double.isInfinite(gamma)) {
            throw new IllegalArgumentException("❗ Gamma must be set to a finite value and must be greater than 0.0! Given: " + gamma);
        }
        renderScheduler.immediate(() -> {
            GLFW.glfwSetGamma(handle, (float) gamma);
            state.updateAndGet(current -> new MonitorState(
                    current.name(), current.position(), current.resolution(), current.refreshRate(),
                    current.size(), current.scale(), current.workArea(), gamma
            ));
        });
    }

    @Override
    public String toString() {
        return "MonitorImpl[" + handle + "]" + "(" +
                "name=" + name() + ", " +
                "position=" + position() + ", " +
                "resolution=" + resolution() + ", " +
                "refreshRate=" + refreshRate() + ", " +
                "size=" + size() + ", " +
                "scale=" + scale() + ", " +
                "screenArea=" + screenArea() + ", " +
                "gamma=" + gamma() + ")";
    }
}