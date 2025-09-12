package com.akjostudios.engine.runtime.impl.monitor;

import com.akjostudios.engine.api.event.EventBus;
import com.akjostudios.engine.api.internal.token.EngineTokens;
import com.akjostudios.engine.api.monitor.Monitor;
import com.akjostudios.engine.api.monitor.MonitorRegistry;
import com.akjostudios.engine.api.monitor.events.MonitorConnectedEvent;
import com.akjostudios.engine.api.monitor.events.MonitorDisconnectedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.akjostudios.engine.runtime.impl.threading.ThreadingImpl.RENDER_THREAD_NAME;

public final class MonitorRegistryImpl implements MonitorRegistry {
    private final List<Monitor> monitors = new CopyOnWriteArrayList<>();

    private final AtomicReference<GLFWMonitorCallback> callback = new AtomicReference<>();

    @Override
    public @NotNull List<Monitor> getMonitors() { return monitors; }

    @Override
    public @NotNull Monitor getPrimaryMonitor() throws IllegalStateException {
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ MonitorRegistry.getPrimaryMonitor() can only be called from the render thread!");
        }
        return Objects.requireNonNull(getMonitorById(GLFW.glfwGetPrimaryMonitor()));
    }

    @Override
    public @Nullable Monitor getMonitorById(long id) {
        return monitors.stream()
                .filter(monitor -> monitor.handle() == id)
                .findFirst().orElse(null);
    }

    /**
     * Initializes the monitor registry and loads all initially connected monitors.
     * @apiNote Must be called by the runtime implementation of the engine AND from the render thread.
     * @throws IllegalCallerException When this method is called externally.
     * @throws IllegalStateException When this method is not called from the render thread.
     */
    @Override
    public void __engine_init(
            @NotNull Object token,
            @NotNull EventBus events
    ) throws IllegalCallerException, IllegalStateException {
        EngineTokens.verify(token);
        if (!Objects.equals(Thread.currentThread().getName(), RENDER_THREAD_NAME)) {
            throw new IllegalStateException("❗ Monitor registry is not being initialized on render thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        monitors.clear();

        PointerBuffer buffer = GLFW.glfwGetMonitors();
        if (buffer == null) { return; }

        for (int i = 0; i < buffer.limit(); i++) {
            monitors.add(new MonitorImpl(buffer.get(i)));
        }

        callback.set(GLFWMonitorCallback.create((handle, event) -> {
            if (event == GLFW.GLFW_CONNECTED) {
                if (monitors.stream().anyMatch(monitor -> monitor.handle() == handle)) { return; }
                Monitor monitor = new MonitorImpl(handle);
                monitors.add(monitor);
                events.publish(new MonitorConnectedEvent(monitor));
            } else if (event == GLFW.GLFW_DISCONNECTED) {
                Monitor monitor = getMonitorById(handle);
                if (monitor == null) { return; }
                monitors.remove(monitor);
                events.publish(new MonitorDisconnectedEvent(monitor));
            }
        }));
        GLFW.glfwSetMonitorCallback(callback.get());
    }

    /**
     * Stops the monitor registry and unloads all connected monitors.
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
            throw new IllegalStateException("❗ Monitor registry is not being stopped on render thread! This is likely a bug in the engine - please report it using the issue tracker.");
        }

        callback.get().free();
        callback.set(null);
        monitors.clear();
    }
}