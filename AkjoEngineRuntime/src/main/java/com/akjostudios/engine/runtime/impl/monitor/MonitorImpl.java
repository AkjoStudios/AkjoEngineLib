package com.akjostudios.engine.runtime.impl.monitor;

import com.akjostudios.engine.api.monitor.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@RequiredArgsConstructor
public final class MonitorImpl implements Monitor {
    private final long handle;

    private double gamma = 1.0;

    @Override
    public long handle() { return handle; }

    @Override
    public @NotNull String name() {
        String name = GLFW.glfwGetMonitorName(handle);
        return name == null ? "Unknown" : name;
    }

    @Override
    public @NotNull ScreenPosition position() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(handle, px, py);
            return new ScreenPosition(px.get(0), py.get(0));
        }
    }

    @Override
    public @NotNull MonitorResolution resolution() {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(handle);
        if (vidmode == null) {
            return new MonitorResolution(0, 0);
        }
        return new MonitorResolution(vidmode.width(), vidmode.height());
    }

    @Override
    public int refreshRate() {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(handle);
        if (vidmode == null) {
            return 0;
        }
        return vidmode.refreshRate();
    }

    @Override
    public @Nullable MonitorSize size() {
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
    public double getGamma() {
        return gamma;
    }

    @Override
    public void setGamma(double gamma) {
        if (gamma <= 0.0 || Double.isInfinite(gamma)) {
            throw new IllegalArgumentException("❗ Gamma must be set to a finite value and must be greater than 0.0! Given: " + gamma);
        }
        GLFW.glfwSetGamma(handle, (float) gamma);
        this.gamma = gamma;
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
                "screenAre=" + screenArea() + ", " +
                "gamma=" + getGamma() + ")";
    }
}